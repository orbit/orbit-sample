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
                packages = listOf("orbit.testClient.actors"),
//                grpcEndpoint = "dns:///localhost:50056/"
                grpcEndpoint = "dns:///orbit-test-server:50056/"
            )
        )

        orbitClient.start()

        val carnival = Carnival(orbitClient)

        Server(port = 8001, carnival = carnival)



        println("started")
    }
}