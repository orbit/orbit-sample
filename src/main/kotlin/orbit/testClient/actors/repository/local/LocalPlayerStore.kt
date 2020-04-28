package orbit.testClient.actors.repository.local

import orbit.testClient.actors.PlayerImpl
import orbit.testClient.actors.repository.PlayerStore

class LocalPlayerStore : PlayerStore {
    private val store = mutableMapOf<String, PlayerImpl>()

    override suspend fun get(): List<PlayerImpl> {
        return store.values.toList()
    }

    override suspend fun get(playerId: String): PlayerImpl? {
        return store[playerId]
    }

    override suspend fun put(playerId: String, player: PlayerImpl) {
        store[playerId] = player
    }
}