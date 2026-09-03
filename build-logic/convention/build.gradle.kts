/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

group = "com.cmgapps.gradle.buildlogic.convention"

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        register("koverConvention") {
            id = "com.cmgapps.kover"
            implementationClass = "com.cmgapps.gradle.Kover"
        }
        register("mavenPublishConvention") {
            id = "com.cmgapps.publish"
            implementationClass = "com.cmgapps.gradle.MavenPublishConvention"
        }
    }
}

dependencies {
    implementation(libs.kover)
    implementation(libs.vanniktech.maven.publish)
}
