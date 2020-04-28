package orbit.testClient.actors.repository

import orbit.testClient.actors.PlayerImpl

interface PlayerStore {
    suspend fun get(): List<PlayerImpl>
    suspend fun get(playerId: String): PlayerImpl?
    suspend fun put(playerId: String, player: PlayerImpl)
}