package orbit.carnival

import orbit.client.OrbitClient
import orbit.client.actor.createProxy
import orbit.carnival.actors.*

class Carnival(val orbit: OrbitClient) {
    suspend fun playGame(gameId: String, playerId: String): PlayedGameResult {
        val player = orbit.actorFactory.createProxy<Player>(playerId)

        return player.playGame(gameId).await()
    }

    suspend fun getPlayer(playerId: String): PlayerResult {
        val player = orbit.actorFactory.createProxy<Player>(playerId)
        val playerDataResult = player.getData().await()

        return PlayerResult(
            playerId = playerId,
            rewards = playerDataResult.rewards
        )
    }

    suspend fun getGame(gameId: String): GameResult {
        val game = orbit.actorFactory.createProxy<Game>(gameId)
        val gameData = game.loadData().await()
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
