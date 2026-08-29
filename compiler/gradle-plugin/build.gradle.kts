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
}

val versionName = project.findProperty("versionName") as String
project.version = versionName

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
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
        type = "String",
        name = "ANNOTATIONS_LIBRARY_COORDINATES",
        expression = "\"${annotationsProject.group}:${annotationsProject.name}:${annotationsProject.version}\"",
    )
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

publishing {
    repositories {
        mavenLocal()
    }
}

dependencies {
    compileOnly(libs.jetbrains.kotlin.gradle.plugin.api)
    compileOnly(libs.jetbrains.kotlin.gradle.plugin)
}
