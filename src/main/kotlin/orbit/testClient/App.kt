/*
 Copyright (C) 2015 - 2019 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

package orbit.testClient

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
import orbit.util.di.ComponentContainer
import orbit.util.di.ExternallyConfigured
import java.time.Duration

fun main() {
    runBlocking {
        delay(Duration.ofSeconds(5))

//        val gameStore = LocalGameStore()
        val gameStore = EtcdGameStore()
//        val playerStore = LocalPlayerStore()
        val playerStore = EtcdPlayerStore()

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

        Server(port = 8001, carnival = carnival)

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