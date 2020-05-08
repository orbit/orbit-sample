package orbit.carnival.actors.repository.local

import orbit.carnival.actors.repository.PlayerRecord
import orbit.carnival.actors.repository.PlayerStore

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