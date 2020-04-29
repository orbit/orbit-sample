package orbit.testClient.actors.repository

import orbit.testClient.actors.GameImpl

interface GameStore {
    suspend fun get(): List<GameImpl>
    suspend fun get(gameId: String): GameImpl?
    suspend fun put(gameId: String, game: GameImpl)
}