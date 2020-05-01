/*
 Copyright (C) 2015 - 2020 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.testClient

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlin.random.Random

class LoadServer(
    carnival: Carnival,
    application: Application
) {
    init {
        application.routing {
            post("/load/play") {
                val body = call.receive<LoadPlayRequest>()

                println("Starting load test: ${body.games} games - ${body.players} players - ${body.count} times - ${body.concurrency} concurrent")

                val games = carnival.getGames()
                val gameCount = Math.max(body.games, games.count())

                val results = (0..body.count).map {i ->
                    carnival.playGame(
                        games[Random.nextInt(1, gameCount)].id,
                        Random.nextInt(1, body.players).toString()
                    )
                }

                call.respond(results)
            }
        }
    }
}

data class LoadPlayRequest(
    val games: Int,
    val players: Int,
    val affinity: Int,
    val count: Int,
    val concurrency: Int
)
