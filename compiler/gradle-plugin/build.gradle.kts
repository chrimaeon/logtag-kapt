import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.plugin)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.pluginPublish)
    id("ktlint")
    id("com.cmgapps.publish")
}

kotlin {
    jvmToolchain(17)
    explicitApi()

    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

buildConfig {
    packageName.set("com.cmgapps.logtag.gradle")
    buildConfigField("String", "LIBRARY_VERSION", "\"${project.version}\"")
    buildConfigField("String", "KOTLIN_VERSION", "\"${embeddedKotlinVersion}\"")

    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")

    val pluginProject = projects.compiler.compilerPlugin
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${pluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${pluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${pluginProject.version}\"")

    val annotationsProject = projects.annotation
    buildConfigField(
        type = String::class.java,
        name = "ANNOTATIONS_LIBRARY_COORDINATES",
        value = "${annotationsProject.group}:${annotationsProject.name}:${annotationsProject.version}",
    )

    try {
        val androidLintLibrary = project(":library")

        buildConfigField(
            type = String::class.java,
            name = "ANDROID_LINT_LIBRARY_COORDINATES",
            value = "${androidLintLibrary.group}:${androidLintLibrary.property("artifactId")}:${androidLintLibrary.version}",
        )
    } catch (e: UnknownProjectException) {
        logger.warn(
            "Could not find :library; Most probably disabled in settings.gradle due to unsupported AGP 9.x in IntelliJ IDEA Android Plugin",
            e,
        )
        buildConfigField(
            type = String::class.java,
            name = "ANDROID_LINT_LIBRARY_COORDINATES",
            value = "com.cmgapps.logtag:log-tag:${project.version}",
        )
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
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.api)
}
