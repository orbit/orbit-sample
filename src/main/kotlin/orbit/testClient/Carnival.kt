package orbit.testClient

import orbit.client.OrbitClient
import orbit.client.actor.createProxy
import orbit.testClient.actors.Game
import orbit.testClient.actors.Player

class Carnival(val orbit: OrbitClient) {
    suspend fun playGame(gameId: String, playerId: String): PlayGameResult {
//        val game = orbit.actorFactory.createProxy<Game>(gameId)
        val player = orbit.actorFactory.createProxy<Player>(playerId)

        val gameResult = player.playGame(gameId).await()

        return PlayGameResult(
            gameId = gameId,
            playerId = playerId,
            name = gameResult.name,
            reward = gameResult.reward,
            timesPlayed = gameResult.timesPlayed
        )
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
        val gameData = game.getData().await()
        return GameResult(
            gameId = gameId,
            name = gameData.name,
            timesPlayed = gameData.timesPlayed
        )
    }
}

data class PlayGameResult(
    val gameId: String,
    val name: String,
    val playerId: String,
    val reward: String,
    val timesPlayed: Int
)


data class GameResult(
    val gameId: String,
    val name: String,
    val timesPlayed: Int
)

data class PlayerResult(
    val playerId: String,
    val rewards: List<String>
)
