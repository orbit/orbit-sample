/*
 Copyright (C) 2015 - 2019 Electronic Arts Inc.  All rights reserved.
 This file is part of the Orbit Project <https://www.orbit.cloud>.
 See license in LICENSE.
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinCoroutinesVersion by extra("1.3.5")
val ktorVersion by extra("1.3.2")
val jacksonVersion by extra("2.10.3")
val orbitVersion by extra("2.0.0-alpha.60")
val grpcVersion by extra("1.28.1")

val mainClass = "orbit.testClient.AppKt"

plugins {
    val kotlinVersion = "1.3.72"

    base
    kotlin("jvm") version kotlinVersion
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    "implementation"(kotlin("stdlib-jdk8"))

    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    "implementation"("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")

    "implementation"("io.ktor:ktor-server-core:$ktorVersion")
    "implementation"("io.ktor:ktor-server-netty:$ktorVersion")
    "implementation"("io.ktor:ktor-gson:$ktorVersion")
    "implementation"("io.ktor:ktor-jackson:$ktorVersion")
    "implementation"("io.ktor:ktor-serialization:$ktorVersion")
    "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    "implementation"("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")

    "implementation"("cloud.orbit:orbit-client:$orbitVersion")

    "implementation"("io.grpc:grpc-netty-shaded:$grpcVersion")
    "implementation"("io.grpc:grpc-protobuf:$grpcVersion")
    "implementation"("io.grpc:grpc-stub:$grpcVersion")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveClassifier.set("release")
    archiveVersion.set("")

    manifest {
        attributes["Implementation-Version"] = project.version
        attributes["Main-Class"] = mainClass
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) {
            it
        } else {
            zipTree(it)
        }
    })
    with(tasks["jar"] as CopySpec)
}