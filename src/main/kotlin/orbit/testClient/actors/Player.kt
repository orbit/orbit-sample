package orbit.testClient.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import orbit.client.actor.ActorWithStringKey

interface Player : ActorWithStringKey {
    fun playGame(gameId: String) : Deferred<String>

}

class PlayerImpl : Player {
    override fun playGame(gameId: String): Deferred<String> {
        return CompletableDeferred("Game played ${gameId}")
    }
}