package orbit.carnival.actors.repository

import orbit.carnival.actors.GameImpl
import orbit.carnival.actors.PlayedGameResult

interface GameStore {
    suspend fun get(): List<GameRecord>
    suspend fun get(gameId: String): GameRecord?
    suspend fun put(game: GameRecord)
}

data class GameRecord(val id: String, val results: List<PlayedGameResult>)

fun GameImpl.toRecord() : GameRecord {
    return GameRecord(this.id, results = this.results)
}

