package orbit.carnival

import orbit.carnival.actors.*
import orbit.client.OrbitClient
import orbit.client.actor.createProxy
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class Carnival(val orbit: OrbitClient) {
    @ExperimentalTime
    suspend fun playGame(gameId: String, playerId: String, gameTime: Duration = 0.seconds): PlayedGameResult {
        val player = orbit.actorFactory.createProxy<Player>(playerId)

        return player.playGame(gameId, gameTime)
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
