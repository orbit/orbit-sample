package orbit.carnival.actors.repository

import orbit.carnival.actors.PlayerImpl

interface PlayerStore {
    suspend fun get(): List<PlayerRecord>
    suspend fun get(id: String): PlayerRecord?
    suspend fun put(player: PlayerRecord)
}

data class PlayerRecord(val id: String, val rewards: List<String>)

fun PlayerImpl.toRecord(): PlayerRecord {
    return PlayerRecord(this.id, rewards = this.rewards)
}
