package orbit.testClient.actors.repository.local

import orbit.testClient.actors.repository.GameRecord
import orbit.testClient.actors.repository.GameStore

class LocalGameStore : GameStore {
    private val store = mutableMapOf<String, GameRecord>()

    suspend override fun get(): List<GameRecord> {
        return store.values.toList()
    }

    suspend override fun get(gameId: String): GameRecord? {
        return store[gameId]
    }

    suspend override fun put(game: GameRecord) {
        store[game.id] = game
    }
}