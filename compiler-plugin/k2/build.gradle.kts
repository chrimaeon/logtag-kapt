/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("jvm")
    id("com.cmgapps.publish")
}

dependencies {
    implementation(projects.compilerPlugin.common)
    compileOnly(libs.jetbrains.compiler.embeddable)
}
