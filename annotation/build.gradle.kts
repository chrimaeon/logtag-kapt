/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.signing)
    alias(libs.plugins.maven.publish)
}

val group: String = project.findProperty("group") as String
val versionName: String = project.findProperty("version") as String

project.group = group
project.version = versionName

publishing {
    publications.named<MavenPublication>("kotlinMultiplatform") {
        groupId = project.group.toString()
        val artifactId: String = project.findProperty("artifactId") as String
        setArtifactId(artifactId)
        version = project.version.toString()

        pom {
            val name: String = project.findProperty("name") as String
            val description: String = project.findProperty("description") as String
            val scmUrl: String = project.findProperty("scmUrl") as String
            val connectionUrl: String = project.findProperty("connectionUrl") as String
            val developerConnectionUrl: String = project.findProperty("developerConnectionUrl") as String
            val projectUrl: String = project.findProperty("projectUrl") as String

            this.name.set(name)
            this.description.set(description)
            this.url.set(projectUrl)
            developers {
                developer {
                    this.id.set("chrimaeon")
                    this.name.set("Christian Grach")
                    this.email.set("christian.grach@cmgapps.com")
                }
            }

            scm {
                this.url.set(scmUrl)
                this.connection.set(connectionUrl)
                this.developerConnection.set(developerConnectionUrl)
            }

            issueManagement {
                this.url.set("$projectUrl/issues")
                this.system.set("github")
            }

            licenses {
                license {
                    this.name.set("Apache-2.0")
                    this.url.set("https://spdx.org/licenses/Apache-2.0.html")
                }
            }
        }

        repositories {
            // TODO setup central sonatype

            mavenLocal()
        }
    }
}

kotlin {
    jvmToolchain(8)

    explicitApi()
    applyDefaultHierarchyTemplate()

    @OptIn(org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation::class)
    abiValidation()

    js().nodejs()

    jvm()

    wasmJs().nodejs()
    wasmWasi().nodejs()

    nativePlatforms()

    sourceSets {
        named("commonMain") {
            dependencies {
                implementation(libs.kotlin.stdlib)
            }
        }
    }
}

private fun KotlinMultiplatformExtension.nativePlatforms() {
    // According to https://kotlinlang.org/docs/native-target-support.html
    // Tier 1
    macosArm64()
    iosSimulatorArm64()
    iosArm64()

    // Tier 2
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosArm64()

    // Tier 3
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    mingwX64()
    watchosDeviceArm64()
    iosX64()
}
