/*
 Copyright (C) 2015 - 2019 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.carnival

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
import orbit.client.OrbitClient
import orbit.client.OrbitClientConfig
import orbit.client.addressable.Addressable
import orbit.client.addressable.AddressableConstructor
import orbit.carnival.actors.GameImpl
import orbit.carnival.actors.PlayerImpl
import orbit.carnival.actors.repository.GameStore
import orbit.carnival.actors.repository.PlayerStore
import orbit.carnival.actors.repository.etcd.EtcdGameStore
import orbit.carnival.actors.repository.etcd.EtcdPlayerStore
import orbit.carnival.actors.repository.local.LocalGameStore
import orbit.carnival.actors.repository.local.LocalPlayerStore
import orbit.util.di.ExternallyConfigured
import org.kodein.di.Instance
import org.kodein.di.Kodein
import org.kodein.di.TT
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.text.DateFormat
import java.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    runBlocking {
        val storeUrl = System.getenv("STORE_URL")

        val kodein = Kodein {
            bind<GameStore>() with singleton { if (storeUrl != null) EtcdGameStore(storeUrl) else LocalGameStore() }
            bind<PlayerStore>() with singleton { if (storeUrl != null) EtcdPlayerStore(storeUrl) else LocalPlayerStore() }
            bind<PlayerImpl>() with provider { PlayerImpl(instance()) }
            bind<GameImpl>() with provider { GameImpl(instance()) }
        }

        val orbitUrl = System.getenv("ORBIT_URL") ?: "dns:///localhost:50056/"
        val orbitClient = OrbitClient(
            OrbitClientConfig(
                namespace = "carnival",
                packages = listOf("orbit.carnival.actors"),
                grpcEndpoint = orbitUrl,
                addressableTTL = Duration.ofSeconds(100),
                addressableConstructor = KodeinAddressableConstructor.KodeinAddressableConstructorSingleton,
                containerOverrides = {
                    instance(kodein)
                }
            )
        )

        println("Connecting to Orbit at $orbitUrl")

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

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() = runBlocking {
                println("Gracefully shutting down")
                orbitClient.stop()
                println("Shutdown complete")
            }
        })

        println("The Carnival has started")
    }
}

class KodeinAddressableConstructor(private val kodein: Kodein) : AddressableConstructor {
    object KodeinAddressableConstructorSingleton : ExternallyConfigured<AddressableConstructor> {
        override val instanceType = KodeinAddressableConstructor::class.java
    }

    override fun constructAddressable(clazz: Class<out Addressable>): Addressable {
        val addressable: Addressable by kodein.Instance(TT(clazz))

        return addressable
    }
}