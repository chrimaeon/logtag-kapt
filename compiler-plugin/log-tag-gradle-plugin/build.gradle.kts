/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    `kotlin-dsl`
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.gradle.pluginPublish)
}

val versionName: String by project
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
}

gradlePlugin {
    plugins {
        create("logTagCompilerSupportPlugin") {
            id = "com.cmgapps.logtag"
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
    implementation(projects.compilerPlugin.common)
    implementation(libs.jetbrains.kotlin.gradle.plugin)
}
