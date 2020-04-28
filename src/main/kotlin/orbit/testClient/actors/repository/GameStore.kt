package orbit.testClient.actors.repository

import orbit.testClient.actors.GameImpl

interface GameStore {
    fun get(): List<GameImpl>
    fun get(gameId: String): GameImpl?
    fun put(gameId: String, game: GameImpl)
}