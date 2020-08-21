package orbit.carnival

import orbit.carnival.actors.*
import orbit.client.OrbitClient
import orbit.client.actor.createProxy

class Carnival(val orbit: OrbitClient) {
    suspend fun playGame(gameId: String, playerId: String, gameTimeMs: Long = 0): PlayedGameResult {
        val player = orbit.actorFactory.createProxy<Player>(playerId)

        return player.playGame(gameId, gameTimeMs)
    }

    suspend fun getPlayer(playerId: String): PlayerResult {
        val player = orbit.actorFactory.createProxy<Player>(playerId)
        val playerDataResult = player.getData()

        return PlayerResult(
            playerId = playerId,
            rewards = playerDataResult.rewards
        )
    }

    suspend fun getGame(gameId: String): GameResult {
        val game = orbit.actorFactory.createProxy<Game>(gameId)
        val gameData = game.loadData()
        return GameResult(
            gameId = gameId,
            name = gameData.name,
            timesPlayed = gameData.timesPlayed
        )
    }

    fun getGames(): List<Catalog.Game> {
        return GameImpl.catalog.games
    }
}

data class GameResult(
    val gameId: String,
    val name: String,
    val timesPlayed: Int
)

data class PlayerResult(
    val playerId: String,
    val rewards: List<String>
)
