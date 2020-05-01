package orbit.testClient.actors.repository.local

import orbit.testClient.actors.repository.PlayerRecord
import orbit.testClient.actors.repository.PlayerStore

class LocalPlayerStore : PlayerStore {
    private val store = mutableMapOf<String, PlayerRecord>()

    override suspend fun get(): List<PlayerRecord> {
        return store.values.toList()
    }

    override suspend fun get(id: String): PlayerRecord? {
        return store[id]
    }

    override suspend fun put(player: PlayerRecord) {
        store[player.id] = player
    }
}