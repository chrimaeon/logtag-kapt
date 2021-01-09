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
    idea
    `java-library`
    kotlin("jvm") version KOTLIN_VERSION
    kotlin("kapt") version KOTLIN_VERSION
    `maven-publish`
    ktlint
}

repositories {
    mavenCentral()
    jcenter()
}

val functionalTestName = "functionalTest"

configurations {
    create("${functionalTestName}Implementation") {
        extendsFrom(testImplementation.get())
    }

    create("${functionalTestName}Runtime") {
        extendsFrom(testRuntime.get())
    }
}

sourceSets {
    create(functionalTestName) {
        java {
            srcDir("src/$functionalTestName/kotlin")
        }

        resources {
            srcDir("src/$functionalTestName/resources")
            outputDir = file("$buildDir/resources/$functionalTestName")
        }

        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

idea {
    module {
        testSourceDirs = testSourceDirs + sourceSets[functionalTestName].allJava.srcDirs
        testResourceDirs = testResourceDirs + sourceSets[functionalTestName].resources.srcDirs
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
        logEvents()
    }

    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = sourceSets[functionalTestName].output.classesDirs
        classpath = sourceSets[functionalTestName].runtimeClasspath
        logEvents()
    }

    check {
        dependsOn(functionalTest)
    }
}

fun Test.logEvents() = testLogging {
    events("PASSED", "SKIPPED", "FAILED")
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
    implementation("com.squareup:javapoet".withVersion())

    compileOnly("com.google.auto.service:auto-service".withVersion())
    kapt("com.google.auto.service:auto-service".withVersion())

    compileOnly("net.ltgt.gradle.incap:incap:0.3")
    kapt("net.ltgt.gradle.incap:incap-processor".withVersion())

    // testImplementation cannot be resoled by dependencyUpdate task
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:3.7.0")
    testImplementation("org.hamcrest:hamcrest-library:2.2")

    "functionalTestImplementation"("junit:junit".withVersion())
    "functionalTestImplementation"("com.github.tschuchortdev:kotlin-compile-testing".withVersion())
}
