package orbit.testClient.actors

import kotlinx.coroutines.*
import orbit.client.actor.ActorWithStringKey
import orbit.client.actor.createProxy
import orbit.client.addressable.AbstractAddressable
import orbit.client.addressable.DeactivationReason
import orbit.client.addressable.OnActivate
import orbit.client.addressable.OnDeactivate
import orbit.shared.addressable.Key
import orbit.testClient.actors.repository.PlayerStore

interface Player : ActorWithStringKey {
    fun getData(): Deferred<PlayerData>
    fun playGame(gameId: String): Deferred<PlayedGameResult>
}

class PlayerImpl(private val playerStore: PlayerStore) : AbstractAddressable(), Player {
    private lateinit var rewards: MutableList<String>

    @OnActivate
    fun onActivate(): Deferred<Unit> {
        val deferred = CompletableDeferred<Unit>()

        val key = (this.context.reference.key as Key.StringKey).key
        GlobalScope.launch {

            val loadedPlayer = playerStore.get(key)

            rewards = when {
                loadedPlayer != null -> loadedPlayer.rewards
                else -> mutableListOf()
            }

            deferred.complete(Unit)
        }

        return deferred
    }

    @OnDeactivate
    fun onDeactivate(deactivationReason: DeactivationReason): Deferred<Unit> {
        println("Deactivating actor ${this.context.reference.key} because ${deactivationReason}")

        return CompletableDeferred(Unit)
    }

    override fun getData(): Deferred<PlayerData> {
        return CompletableDeferred(PlayerData(rewards = this.rewards))
    }

    override fun playGame(gameId: String): Deferred<PlayedGameResult> = GlobalScope.async {
        val playerId = (context.reference.key as Key.StringKey).key
        val game = this@PlayerImpl.context.client.actorFactory.createProxy<Game>(gameId)

        val result = game.play(playerId).await()
        if (result.winner) {
            this@PlayerImpl.rewards.add(result.reward)
        }
        return@async result
    }
}

data class PlayerData(
    val rewards: List<String>
)
