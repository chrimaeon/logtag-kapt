/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.file.Paths

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("io.arrow-kt.arrow")
    `maven-publish`
    signing
    ktlint
    id("org.jetbrains.dokka") version "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin".version()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

val pubName = "compilerPlugin"

publishing {
    publications {
        register<MavenPublication>(pubName) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

signing {
    sign(publishing.publications[pubName])
}

tasks.jar {
    from(
        zipTree(sourceSets.main.get().compileClasspath.find {
            it.absolutePath.contains(Paths.get("arrow-kt", "compiler-plugin").toString())
        } ?: error("arrow not found"))
    ) {
        exclude("META-INF/services/org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar")
    }
}

dependencies {
    addCompilerPluginDependencies()
}
