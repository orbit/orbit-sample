package orbit.testClient.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import orbit.client.actor.ActorWithStringKey

interface Player : ActorWithStringKey {
    fun award(gameId: String, prize: String): Deferred<String>
    fun getData(): Deferred<PlayerData>
}

class PlayerImpl : Player {
    private val rewards = mutableListOf<String>()

    override fun award(gameId: String, reward: String): Deferred<String> {
        rewards.add(reward)
        return CompletableDeferred("Received ${reward} from game ${gameId}")
    }

    override fun getData(): Deferred<PlayerData> {
        return CompletableDeferred(PlayerData(rewards = this.rewards))
    }
}

data class PlayerData(
    val rewards: List<String>
)
