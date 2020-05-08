/*
 Copyright (C) 2015 - 2020 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.carnival

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import orbit.carnival.actors.GameImpl

class Server(
    carnival: Carnival,
    application: Application
) {
    init {
        application.routing {
            get("/") {
                call.respondText("Ok", ContentType.Text.Plain)
            }

            get("/games") {
                call.respond(GameImpl.catalog.games)
            }

            get("/game/{gameId}") {
                val gameId = call.parameters["gameId"]
                if (gameId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                call.respond(carnival.getGame(gameId))
            }

            get("/player/{playerId}") {
                val playerId = call.parameters["playerId"]
                if (playerId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                call.respond(carnival.getPlayer(playerId))
            }

            post("/player/{playerId}/play") {
                val playerId = call.parameters["playerId"]
                if (playerId == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                val body = call.receive<PlayGameRequest>()
                val gameId = body.game
                println("Player ${playerId} playing game: ${gameId}")

                val result = carnival.playGame(gameId, playerId)

                call.respond(result)
            }
        }
    }
}


data class PlayGameRequest(
    val game: String
)
