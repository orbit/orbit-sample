/*
 Copyright (C) 2015 - 2020 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.testClient

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import orbit.testClient.actors.GameImpl
import java.text.DateFormat

class Server(
    port: Int = 8080,
    carnival: Carnival
) {
    init {
        embeddedServer(Netty, port) {
            install(DefaultHeaders)

            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    dateFormat = DateFormat.getDateInstance()
                    deactivateDefaultTyping()
                }
            }

            routing {
                get("/") {
                    call.respondText("Cool Ok", ContentType.Text.Plain)
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
                    println("Player ${playerId} playing gamed: ${gameId}")

                    val result = carnival.playGame(gameId, playerId)

                    call.respond(result)
                }
            }
        }.start()
    }
}

data class PlayGameRequest(
    val game: String
)
