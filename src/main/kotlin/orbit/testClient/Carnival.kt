package orbit.testClient

import orbit.client.OrbitClient
import orbit.client.actor.createProxy
import orbit.testClient.actors.Game
import orbit.testClient.actors.Player

class Carnival(val orbit: OrbitClient) {
    suspend fun playGame(gameId: String, playerId: String): PlayGameResult {
        val game = orbit.actorFactory.createProxy<Game>(gameId)
        val player = orbit.actorFactory.createProxy<Player>(playerId)

        val gameResult = game.play(playerId).await()
        val playerResult = player.award(gameId, gameResult.reward).await()

        return PlayGameResult(
            gameId = gameId,
            playerId = playerId,
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
}

data class PlayGameResult(
    val gameId: String,
    val playerId: String,
    val reward: String,
    val timesPlayed: Int
)

data class PlayerResult(
    val playerId: String,
    val rewards: List<String>
)
