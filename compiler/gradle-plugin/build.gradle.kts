/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.gradle.plugin)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.pluginPublish)
    id("ktlint")
    id("com.cmgapps.publish")
}

private val jvmTargetVersion = JvmTarget.JVM_17

kotlin {
    jvmToolchain(jvmTargetVersion.target.toInt())
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

val minimumGradleVersion = "9.1.0" // minimum for AGP 9.0.0
configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradlePluginApiVersion::class.java, minimumGradleVersion),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTargetVersion.target
    targetCompatibility = jvmTargetVersion.target
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
        jvmTarget = jvmTargetVersion
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(platform(libs.junit.bom))
                implementation(libs.junit.jupiter) {
                    exclude(group = "org.hamcrest")
                }
                implementation(libs.hamcrest)
                implementation(gradleTestKit())
                implementation(libs.java.diff.utils)
            }

            targets.all {
                testTask.configure {
                    javaLauncher.set(
                        javaToolchains.launcherFor {
                            languageVersion.set(JavaLanguageVersion.of(21))
                        },
                    )
                    jvmArgs("-Xmx2g", "-Xms512m")
                    testLogging {
                        events("PASSED", "SKIPPED", "FAILED")
                    }

                    dependsOn(
                        ":annotation:publishAllPublicationsToLocalStagingRepository",
                        ":compiler:compiler-plugin:publishAllPublicationsToLocalStagingRepository",
                        ":compiler:gradle-plugin:publishAllPublicationsToLocalStagingRepository",
                        ":android-lint:publishAllPublicationsToLocalStagingRepository",
                    )
                }
            }
        }
    }
}

buildConfig {
    packageName.set("com.cmgapps.logtag.gradle")
    buildConfigField("MIN_KOTLIN_VERSION", "2.3.0")
    buildConfigField("MAX_KOTLIN_VERSION", libs.versions.kotlin)
    buildConfigField("LIBRARY_VERSION", project.version.toString())

    buildConfigField("KOTLIN_PLUGIN_ID", rootProject.group.toString())

    val pluginProject = projects.compiler.compilerPlugin
    buildConfigField("KOTLIN_PLUGIN_GROUP", pluginProject.group.toString())
    buildConfigField("KOTLIN_PLUGIN_NAME", pluginProject.name)

    val annotationsProject = projects.annotation
    buildConfigField(
        "ANNOTATIONS_LIBRARY_COORDINATES",
        "${annotationsProject.group}:${annotationsProject.name}:${annotationsProject.version}",
    )

    try {
        val androidLintLibrary = project(":android-lint")

        buildConfigField(
            "ANDROID_LINT_LIBRARY_COORDINATES",
            "${androidLintLibrary.group}:${androidLintLibrary.name}:${androidLintLibrary.version}",
        )
    } catch (e: UnknownProjectException) {
        logger.warn(
            "Could not find :android-lint; Most probably disabled in settings.gradle due to unsupported AGP 9.x in IntelliJ IDEA Android Plugin",
            e,
        )
        buildConfigField(
            "ANDROID_LINT_LIBRARY_COORDINATES",
            "com.cmgapps.logtag:android-lint:${project.version}",
        )
    }

    sourceSets.named("test") {
        useKotlinOutput {
            packageName = "com.cmgapps.logtag.gradle"
            topLevelConstants = true
        }
        buildConfigField("MINIMUM_GRADLE_VERSION", minimumGradleVersion)
    }
}

gradlePlugin {
    plugins {
        create("logTagGradlePlugin") {
            id = rootProject.group.toString()
            displayName = "LogTag Gradle Plugin"
            description = "Gradle plugin for LogTag"
            tags =
                setOf("Kotlin Compiler Plugin", "compiler", "log-tag", "logging", "tagging")
            implementationClass = "com.cmgapps.gradle.LogTagGradleSupportPlugin"
        }
    }

    testSourceSets(sourceSets["test"])
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.api)
}
