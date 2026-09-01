/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.util.Date

plugins {
    alias(libs.plugins.gradle.idea)
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    kotlin("kapt")
    id("com.cmgapps.publish")
    id("ktlint")
    alias(libs.plugins.dokka)
    id("com.cmgapps.kover")
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test =
            named("test", JvmTestSuite::class) {
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

    val functionalTest =
        named("functionalTest", Test::class) {
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

dependencies {
    implementation(projects.annotation)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.javapoet)

    compileOnly(libs.google.ksp.api)
}
