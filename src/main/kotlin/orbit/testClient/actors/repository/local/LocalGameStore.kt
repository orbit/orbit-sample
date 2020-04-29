package orbit.testClient.actors.repository.local

import orbit.testClient.actors.GameImpl
import orbit.testClient.actors.repository.GameStore

class LocalGameStore : GameStore {
    private val store = mutableMapOf<String, GameImpl>()

    suspend override fun get(): List<GameImpl> {
        return store.values.toList()
    }

    suspend override fun get(gameId: String): GameImpl? {
        return store[gameId]
    }

    suspend override fun put(gameId: String, game: GameImpl) {
        store[gameId] = game
    }
}