package orbit.carnival.actors

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import orbit.client.actor.AbstractActor
import orbit.client.actor.ActorWithStringKey
import orbit.client.actor.createProxy
import orbit.client.addressable.DeactivationReason
import orbit.client.addressable.OnActivate
import orbit.client.addressable.OnDeactivate
import orbit.shared.addressable.Key
import orbit.carnival.actors.repository.PlayerStore
import orbit.carnival.actors.repository.toRecord

interface Player : ActorWithStringKey {
    fun getData(): Deferred<PlayerData>
    fun playGame(gameId: String): Deferred<PlayedGameResult>
}

class PlayerImpl(private val playerStore: PlayerStore) : AbstractActor(), Player {
    internal lateinit var rewards: MutableList<String>

    val id: String get() = (this.context.reference.key as Key.StringKey).key

    @OnActivate
    fun onActivate(): Deferred<Unit> = GlobalScope.async {
        println("Activating player ${id}")

        loadFromStore()
    }

    @OnDeactivate
    fun onDeactivate(deactivationReason: DeactivationReason): Deferred<Unit> = GlobalScope.async {
        println("Deactivating player ${id} because ${deactivationReason}")
        saveToStore()
    }

    private suspend fun loadFromStore() {
        val loadedPlayer = playerStore.get(id)

        rewards = loadedPlayer?.rewards?.toMutableList() ?: mutableListOf()
    }

    private suspend fun saveToStore() {
        playerStore.put(this.toRecord())
    }

    override fun getData(): Deferred<PlayerData> = GlobalScope.async {
        return@async PlayerData(rewards = rewards)
    }

    override fun playGame(gameId: String): Deferred<PlayedGameResult> = GlobalScope.async {
        val playerId = (context.reference.key as Key.StringKey).key
        val game = context.client.actorFactory.createProxy<Game>(gameId)

        val result = game.play(playerId).await()
        if (result.winner) {
            this@PlayerImpl.rewards.add(result.reward)
        }

        println("Player $id played game $gameId. Prize: ${result.reward}")

        saveToStore()

        return@async result
    }
}

data class PlayerData(
    val rewards: List<String>
)
