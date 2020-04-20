package orbit.testClient.actors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import orbit.client.actor.ActorWithStringKey
import orbit.client.addressable.AbstractAddressable
import orbit.client.addressable.OnActivate
import orbit.shared.addressable.Key
import java.io.File
import kotlin.random.Random

interface Game : ActorWithStringKey {
    fun play(playerId: String): Deferred<PlayedGameResult>
    fun getData(): Deferred<GameData>
}

class GameImpl : AbstractAddressable(), Game {
    private lateinit var gameData: Catalog.Game

    private val baseWinningOdds = .5

    private var results = mutableListOf<PlayedGameResult>()

    @OnActivate
    fun onActivate(): Deferred<Unit> {
        gameData = catalog.games.firstOrNull() { game ->
            game.id == (context.reference.key as Key.StringKey).key
        } ?: throw IllegalArgumentException("Game does not exist")

        return CompletableDeferred(Unit)
    }

    companion object {
        @JvmStatic
        var catalog: Catalog

        init {
            var gamesFile = "src/main/resources/games.yml"
            catalog = ObjectMapper(YAMLFactory()).readValue(File(gamesFile))
        }
    }

    override fun play(playerId: String): Deferred<PlayedGameResult> {

        val previousResult = this.results.lastOrNull()
        val replay = previousResult?.playerId == playerId && previousResult?.level < 4
        var level = if (replay) previousResult!!.level else 0

        val win = Random.nextDouble() < (baseWinningOdds / (level + 1))
        if (win) level++

        val prize = if (win)
            (when (level) {
                1 -> gameData.prizes.small
                2 -> gameData.prizes.medium
                3 -> gameData.prizes.large
                4 -> gameData.prizes.grand
                else -> listOf()
            }).random() else ""

        val result = PlayedGameResult(
            name = this.gameData.name,
            playerId = playerId,
            winner = win,
            reward = prize,
            level = level
        )

        results.add(result)

        return CompletableDeferred(result)
    }

    override fun getData(): Deferred<GameData> {
        return CompletableDeferred(
            GameData(
                name = this.gameData.name,
                timesPlayed = this.results.count()
            )
        )
    }
}

data class Catalog(val games: List<Game> = listOf()) {
    data class Game(
        val id: String = "",
        val name: String = "",
        val theme: String = "",
        val prizes: PrizeList = PrizeList()
    )

    data class PrizeList(
        val small: List<String> = listOf(),
        val medium: List<String> = listOf(),
        val large: List<String> = listOf(),
        val grand: List<String> = listOf()
    )
}

data class GameData(
    val name: String,
    val timesPlayed: Int
)

data class PlayedGameResult(
    val name: String,
    val playerId: String,
    val winner: Boolean,
    val level: Int,
    val reward: String
)
