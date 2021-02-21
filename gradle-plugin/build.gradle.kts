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

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("com.squareup:kotlinpoet".withVersion())
    }
}

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    `maven-publish`
    signing
    ktlint
    id("org.jetbrains.dokka") version "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin".version()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val group: String by project
val versionName: String by project
val pomName: String by project
val pomDescription: String by project

val pubName = "pluginMaven"

gradlePlugin {
    plugins {
        register(pubName) {
            id = "com.cmgapps.kotlin.logtag"
            displayName = pomName
            implementationClass = "com.cmgapps.gradle.LogTagPlugin"
            description = pomDescription
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
        register<MavenPublication>(pubName) {
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

signing {
    sign(publishing.publications[pubName])
}

dependencies {
    addGradlePluginDependencies()
}

val generatedDirPath = "generated/source/build-properties/kotlin/main"

sourceSets {
    main {
        java.srcDir(buildDir.resolve(generatedDirPath))
    }
}

tasks {
    val generateBuildProperties by registering {
        val buildPropertiesDir = project.buildDir.resolve(generatedDirPath)

        inputs.property("version", versionName)
        inputs.property("group", project.group)

        outputs.dir(buildPropertiesDir)

        doLast {
            val modifier = listOf(KModifier.INTERNAL, KModifier.CONST)
            val properties = mutableListOf<PropertySpec.Builder>()
            properties += PropertySpec.builder("VERSION", String::class, modifier).apply {
                mutable(false)
                initializer("%S", versionName).build()
            }
            properties += PropertySpec.builder("GROUP", String::class, modifier).apply {
                mutable(false)
                initializer("%S", project.group).build()
            }

            FileSpec.builder("com.cmgapps.gradle", "BuildProperties").apply {
                properties.forEach {
                    addProperty(it.build())
                }
            }.build()
                .writeTo(buildPropertiesDir)
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateBuildProperties)
    }
}
