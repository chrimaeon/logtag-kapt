/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.cmgapps.publish")
}

dependencies {
    implementation(projects.compilerPlugin.common)
    implementation(projects.compilerPlugin.backend)
    implementation(projects.compilerPlugin.k2)
    compileOnly(libs.jetbrains.compiler.embeddable)
    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice.autoservice)

    testImplementation(libs.jetbrains.compiler.embeddable)
    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}
