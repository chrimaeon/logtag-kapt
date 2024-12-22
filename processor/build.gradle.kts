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

import java.util.Date

plugins {
    idea
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("com.cmgapps.publish")
    id("ktlint")
    alias(libs.plugins.dokka)
}

val functionalTestName = "functionalTest"

val functionalTestConfiguration =
    configurations.create("${functionalTestName}Implementation") {
        extendsFrom(configurations.testImplementation.get())
    }

sourceSets {
    create(functionalTestName) {
        java {
            srcDir("src/$functionalTestName/kotlin")
        }

        resources {
            srcDir("src/$functionalTestName/resources")
            destinationDirectory.set(layout.buildDirectory.dir("resources/$functionalTestName"))
        }

        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

idea {
    module {
        testSources.setFrom(testSources, sourceSets[functionalTestName].allJava.srcDirs)
        testResources.setFrom(testResources, sourceSets[functionalTestName].resources.srcDirs)
    }
}

kotlin {
    explicitApi()
    jvmToolchain(17)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = sourceSets[functionalTestName].output.classesDirs
        classpath = sourceSets[functionalTestName].runtimeClasspath
        useJUnitPlatform()

        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    check {
        dependsOn(functionalTest)
    }

    jar {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Built-By" to System.getProperty("user.name"),
                "Built-Date" to Date(),
                "Built-JDK" to System.getProperty("java.version"),
                "Built-Gradle" to gradle.gradleVersion,
            )
        }
    }

    // koverVerify {
    //     rule {
    //         name = "Minimal line coverage"
    //         bound {
    //             minValue = 80
    //             valueType = COVERED_LINES_PERCENTAGE
    //         }
    //     }
    // }
}

dependencies {
    implementation(project(":annotation"))

    compileOnly(libs.google.ksp.api)

    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.javapoet)

    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice.autoservice)

    compileOnly(libs.ltgt.incap.incap)
    kapt(libs.ltgt.incap.processor)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.hamcrest)

    functionalTestConfiguration(platform(libs.junit.bom))
    functionalTestConfiguration(libs.junit.jupiter)
    functionalTestConfiguration(libs.tschuchortdev.compile.testing.ksp)
    functionalTestConfiguration(libs.google.ksp.api)
    functionalTestConfiguration(libs.google.ksp.processor)
    functionalTestConfiguration(libs.jetbrains.compiler.embeddable)
}
