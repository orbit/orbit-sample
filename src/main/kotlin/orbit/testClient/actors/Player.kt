package orbit.testClient.actors

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import orbit.client.actor.ActorWithStringKey
import orbit.client.actor.createProxy
import orbit.client.addressable.AbstractAddressable
import orbit.shared.addressable.Key

interface Player : ActorWithStringKey {
    fun getData(): Deferred<PlayerData>
    fun playGame(gameId: String): Deferred<PlayedGameResult>
}

class PlayerImpl : AbstractAddressable(), Player {
    private val rewards = mutableListOf<String>()

    override fun getData(): Deferred<PlayerData> {
        return CompletableDeferred(PlayerData(rewards = this.rewards))
    }

    override fun playGame(gameId: String): Deferred<PlayedGameResult> = GlobalScope.async {
        val playerId = (context.reference.key as Key.StringKey).key
        val game = this@PlayerImpl.context.client.actorFactory.createProxy<Game>(gameId)

        val result = game.play(playerId).await()

        this@PlayerImpl.rewards.add(result.reward)
        return@async result
    }
}

data class PlayerData(
    val rewards: List<String>
)
