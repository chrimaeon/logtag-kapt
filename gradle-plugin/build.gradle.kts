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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    `maven-publish`
    signing
    ktlint
    id("org.jetbrains.dokka") version "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin".version()
}

val group: String by project
val versionName: String by project
val pomName: String by project
val description: String by project
val pluginDesc = description

project.group = group
project.version = versionName

gradlePlugin {
    plugins {
        register("pluginMaven") {
            id = "com.cmgapps.kotlin.logtag"
            displayName = pomName
            implementationClass = "com.cmgapps.gradle.LogTagPlugin"
            description = pluginDesc
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(tasks.dokkaJavadoc)
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {

            artifact(sourcesJar.get())
            artifact(javadocJar.get())
            logtagPom(project)
        }
    }
    repositories {
        sonatype(project)
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}

dependencies {
    addGradlePluginDependencies()
}

val generatedDirPath = "generated/source/build-properties/kotlin/main"

sourceSets {
    main.get().java.srcDirs("$buildDir/$generatedDirPath")
}

tasks {
    val generateBuildProperties by registering {
        val buildPropertiesFile = project.buildDir.resolve(generatedDirPath).resolve("BuildProperties.kt")

        inputs.property("version", versionName)
        inputs.property("group", project.group)

        outputs.file(buildPropertiesFile)

        doLast {
            buildPropertiesFile.parentFile.mkdirs()
            buildPropertiesFile.writeText(
                """
                    package com.cmgapps.gradle

                    internal const val VERSION = "$versionName"
                    internal const val GROUP = "${project.group}"
                """.trimIndent()
            )
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateBuildProperties)
    }
}
