package orbit.testClient.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import orbit.client.actor.ActorWithStringKey

interface Game : ActorWithStringKey {
    fun play(playerId: String?): Deferred<GameResult>
}

class GameImpl : Game {
    var timesPlayed = 0
    override fun play(playerId: String?): Deferred<GameResult> {
        return CompletableDeferred(
            GameResult(
                timesPlayed = ++timesPlayed,
                reward = "No prize"
            )
        )
    }
}

data class GameResult(
    val timesPlayed: Int,
    val reward: String
)
