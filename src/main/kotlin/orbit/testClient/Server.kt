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
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import orbit.client.OrbitClient
import orbit.client.actor.createProxy
import orbit.testClient.actors.Game
import java.text.DateFormat

class Server(
    port: Int = 8080,
    orbitClient: OrbitClient
) {
    init {
        embeddedServer(Netty, port) {
            install(DefaultHeaders)

            install(ContentNegotiation) {
//                json(
//                    contentType = ContentType.Application.Json,
//                    json = Json(
//                        DefaultJsonConfiguration.copy(
//                            prettyPrint = true
//                        )
//                    )
//                )
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
                    call.respondText(contentType = ContentType.Text.Plain) {
                        "Games"
                    }
                }

                post("/player/{playerId}/play") {
                    println("request")
                    val playerId = call.parameters["playerId"]
                    val body = call.receive<PlayGameRequest>()
                    val gameId = body.game
                    println("Player ${playerId} playing gamed: ${gameId}")

                    val game = orbitClient.actorFactory.createProxy<Game>(gameId)

                    runBlocking {
                        val result = game.play(playerId).await()

                        call.respond(
                            PlayGameResponse(
                                player = playerId,
                                game = gameId,
                                timesPlayed = result.timesPlayed,
                                prize = result.prize
                            )
                        )
                    }
                }
            }
        }.start()
    }
}

@Serializable
data class PlayGameRequest(
    val game: String
)

@Serializable
data class PlayGameResponse(
    val player: String?,
    val game: String?,
    val timesPlayed: Int,
    val prize: String? = "Nothing"
)
