package orbit.carnival.actors

import kotlinx.coroutines.delay
import orbit.carnival.actors.repository.PlayerStore
import orbit.carnival.actors.repository.toRecord
import orbit.client.actor.AbstractActor
import orbit.client.actor.ActorWithStringKey
import orbit.client.actor.createProxy
import orbit.client.addressable.DeactivationReason
import orbit.client.addressable.OnActivate
import orbit.client.addressable.OnDeactivate
import orbit.shared.addressable.Key
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

interface Player : ActorWithStringKey {
    suspend fun getData(): PlayerData
    @ExperimentalTime
    suspend fun playGame(gameId: String, gameTime: Duration): PlayedGameResult
}

class PlayerImpl(private val playerStore: PlayerStore) : AbstractActor(), Player {
    internal lateinit var rewards: MutableList<String>

    val id: String get() = (this.context.reference.key as Key.StringKey).key

    @OnActivate
    suspend fun onActivate() {
        loadFromStore()
    }

    @OnDeactivate
    suspend fun onDeactivate(deactivationReason: DeactivationReason) {
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

    @ExperimentalTime
    override suspend fun playGame(gameId: String, gameTime: Duration): PlayedGameResult {
        val playerId = (context.reference.key as Key.StringKey).key
        val game = context.client.actorFactory.createProxy<Game>(gameId)

        val result = game.play(playerId, gameTime)
        if (result.winner) {
            this@PlayerImpl.rewards.add(result.reward)
        }

        saveToStore()

        return result
    }
}

data class PlayerData(
    val rewards: List<String>
)
