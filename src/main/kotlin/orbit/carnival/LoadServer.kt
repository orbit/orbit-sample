/*
 Copyright (C) 2015 - 2020 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.carnival

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import java.time.Duration
import java.time.Instant
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

@ExperimentalTime
class LoadServer(
    carnival: Carnival,
    application: Application
) {
    private var testInProgress = false

    init {
        application.routing {
            post("/load/play") {
                val startTime = Instant.now()
                val body = call.receive<LoadPlayRequest>()
                if (!body.concurrent && testInProgress) {
                    call.respond("Test in progress")
                    return@post
                }
                println("Starting load test: ${body.games} games - ${body.players} players - ${body.count} times")
                testInProgress = true
                val games = carnival.getGames()
                val gameCount = Math.min(body.games, games.count())

                val failures = mutableListOf<String>()
                val results = (0..body.count).map { _ ->
                    val gameId = games[Random.nextInt(0, gameCount)].id
                    val playerId = "${Random.nextInt(1, body.players + 1)}"
                    try {
                        carnival.playGame(
                            gameId,
                            playerId,
                            body.gameTimeMs.milliseconds
                        )
                    } catch (e: Throwable) {
                        val failure = "Failure Game($gameId): Player $playerId: \n${e.message}"
                        println(failure)
                        failures.add(failure)
                        null
                    }
                }.filterNotNull()

                println("--- Load test complete in ${Duration.between(Instant.now(), startTime).seconds} seconds ---")
                println(failures.joinToString("\n"))
                testInProgress = false
                call.respond(object {
                    var gamesPlayed = results.count()
                    var winners = results.count { r -> r.winner }
                })
            }
        }
    }
}

data class LoadPlayRequest(
    val games: Int,
    val players: Int,
    val count: Int,
    val concurrent: Boolean = true,
    val gameTimeMs: Int = 0
)
