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
import java.time.Duration


fun main() {
    runBlocking {
        delay(Duration.ofSeconds(5))

        val orbitClient = OrbitClient(
            OrbitClientConfig(
                namespace = "carnival",
                packages = listOf("orbit.testClient.actors")
            )
        )

        orbitClient.start()

        Server(port = 8001, orbitClient = orbitClient)



        println("started")
    }
}