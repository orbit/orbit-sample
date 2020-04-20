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

interface Game : ActorWithStringKey {
    fun play(playerId: String): Deferred<PlayedGameResult>
    fun getData(): Deferred<GameData>
}

class GameImpl : AbstractAddressable(), Game {
    var timesPlayed = 0

    private lateinit var gameData: Catalog.Game

    @OnActivate
    fun onActivate(): Deferred<Unit> {
        val game = catalog.games.firstOrNull() { game ->
            game.id == (this.context.reference.key as Key.StringKey).key
        }

        if (game == null) {
            throw IllegalArgumentException("Game does not exist")
        }

        gameData = game
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
        return CompletableDeferred(
            PlayedGameResult(
                name = this.gameData.name,
                timesPlayed = ++timesPlayed,
                reward = "Ninja Necklace"
            )
        )
    }

    override fun getData(): Deferred<GameData> {
        return CompletableDeferred(
            GameData(
                name = this.gameData.name,
                timesPlayed = this.timesPlayed
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
    val timesPlayed: Int,
    val reward: String
)
