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

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.util.Date

plugins {
    idea
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("com.cmgapps.publish")
    id("ktlint")
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(platform(libs.junit.bom))
                implementation(libs.junit.jupiter)
                implementation(libs.mockito.junit)
                implementation(libs.mockito.kotlin)
                implementation(libs.hamcrest)
                runtimeOnly(libs.junit.platform)
            }
        }

        register<JvmTestSuite>("functionalTest") {
            useJUnitJupiter()
            dependencies {
                implementation(platform(libs.junit.bom))
                implementation(libs.junit.jupiter)
                implementation(project())
                implementation(libs.hamcrest)
                implementation(libs.tschuchortdev.compile.testing.ksp)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
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
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    val functionalTest by getting(Test::class) {
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
}

kover {
    useJacoco()
    reports {
        verify {
            rule("Minimal line coverage") {
                bound {
                    minValue = 80
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
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
}
