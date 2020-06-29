package orbit.carnival.actors

import orbit.carnival.actors.repository.PlayerStore
import orbit.carnival.actors.repository.toRecord
import orbit.client.actor.AbstractActor
import orbit.client.actor.ActorWithStringKey
import orbit.client.actor.createProxy
import orbit.client.addressable.DeactivationReason
import orbit.client.addressable.OnActivate
import orbit.client.addressable.OnDeactivate
import orbit.shared.addressable.Key

interface Player : ActorWithStringKey {
    suspend fun getData(): PlayerData
    suspend fun playGame(gameId: String): PlayedGameResult
}

class PlayerImpl(private val playerStore: PlayerStore) : AbstractActor(), Player {
    internal lateinit var rewards: MutableList<String>

    val id: String get() = (this.context.reference.key as Key.StringKey).key

    @OnActivate
    suspend fun onActivate() {
        println("Activating player ${id}")

        loadFromStore()
    }

    @OnDeactivate
    suspend fun onDeactivate(deactivationReason: DeactivationReason) {
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

    override suspend fun getData(): PlayerData {
        return PlayerData(rewards = rewards)
    }

    override suspend fun playGame(gameId: String): PlayedGameResult {
        val playerId = (context.reference.key as Key.StringKey).key
        val game = context.client.actorFactory.createProxy<Game>(gameId)

        val result = game.play(playerId)
        if (result.winner) {
            this@PlayerImpl.rewards.add(result.reward)
        }

        println("Player $id played game $gameId. Prize: ${result.reward}")

        saveToStore()

        return result
    }
}

data class PlayerData(
    val rewards: List<String>
)
