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
    kotlin("jvm") version KOTLIN_VERSION
    kotlin("kapt") version KOTLIN_VERSION
    `maven-publish`
    ktlint
}

repositories {
    mavenCentral()
    jcenter()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    test {
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }
}

val group: String by project
val versionName: String by project

project.group = group
project.version = versionName

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {

            from(components["java"])

            val artifactId: String by project
            val name: String by project
            val description: String by project
            val scmUrl: String by project
            val connectionUrl: String by project
            val developerConnectionUrl: String by project

            this.groupId = project.group.toString()
            this.artifactId = artifactId
            this.version = project.version.toString()

            pom {
                this.name.set(name)
                this.description.set(description)
                developers {
                    developer {
                        this.id.set("cgrach")
                        this.name.set("Christian Grach")
                    }
                }

                scm {
                    this.url.set(scmUrl)
                    this.connection.set(connectionUrl)
                    this.developerConnection.set(developerConnectionUrl)
                }
            }
        }
    }
}

dependencies {
    implementation(project(":annotation"))

    implementation(kotlin("stdlib", KOTLIN_VERSION))
    implementation("com.squareup:kotlinpoet".withVersion())

    compileOnly("com.google.auto.service:auto-service".withVersion())
    kapt("com.google.auto.service:auto-service".withVersion())

    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    kapt("net.ltgt.gradle.incap:incap-processor".withVersion())

    testImplementation("junit:junit".withVersion())
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing".withVersion())
}
