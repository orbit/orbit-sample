/*
 Copyright (C) 2015 - 2019 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.testClient

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.delay
import orbit.client.OrbitClient
import orbit.client.OrbitClientConfig
import orbit.client.addressable.Addressable
import orbit.client.addressable.AddressableConstructor
import orbit.testClient.actors.GameImpl
import orbit.testClient.actors.PlayerImpl
import orbit.testClient.actors.repository.GameStore
import orbit.testClient.actors.repository.PlayerStore
import orbit.testClient.actors.repository.etcd.EtcdGameStore
import orbit.testClient.actors.repository.etcd.EtcdPlayerStore
import orbit.testClient.actors.repository.local.LocalGameStore
import orbit.testClient.actors.repository.local.LocalPlayerStore
import orbit.util.di.ComponentContainer
import orbit.util.di.ExternallyConfigured
import java.text.DateFormat
import java.time.Duration

fun main() {
    runBlocking {
        val storeUrl = System.getenv("STORE_URL")

        val gameStore = if (storeUrl != null) EtcdGameStore(storeUrl) else LocalGameStore()
        val playerStore = if (storeUrl != null) EtcdPlayerStore(storeUrl) else LocalPlayerStore()

        val orbitClient = OrbitClient(
            OrbitClientConfig(
                namespace = "carnival",
                packages = listOf("orbit.testClient.actors"),
                grpcEndpoint = System.getenv("ORBIT_URL") ?: "dns:///localhost:50056/",
                addressableTTL = Duration.ofSeconds(10),
                addressableConstructor = RepositoryAddressableConstructor.RepositoryAddressableConstructorSingleton,
                containerOverrides = {
                    instance<GameStore>(gameStore)
                    instance<PlayerStore>(playerStore)

                    definition<PlayerImpl>()
                    definition<GameImpl>()
                }
            )
        )

        orbitClient.start()

        val carnival = Carnival(orbitClient)

        embeddedServer(Netty, 8001) {
            install(DefaultHeaders)

            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    dateFormat = DateFormat.getDateInstance()
                    deactivateDefaultTyping()
                }
            }

            install(StatusPages) {
                exception<Throwable> { cause ->
                    println("Exception: ${cause}")
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            Server(carnival, this)
            LoadServer(carnival, this)

        }.start()

        println("Test Client Started")
    }
}

class RepositoryAddressableConstructor(private val container: ComponentContainer) : AddressableConstructor {
    object RepositoryAddressableConstructorSingleton : ExternallyConfigured<AddressableConstructor> {
        override val instanceType = RepositoryAddressableConstructor::class.java
    }

    override fun constructAddressable(clazz: Class<out Addressable>): Addressable {
        return container.resolve(clazz)
    }
}