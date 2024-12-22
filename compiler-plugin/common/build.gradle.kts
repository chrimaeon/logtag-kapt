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
    api(libs.jetbrains.kotlin.stdlib8)
    compileOnly(libs.jetbrains.compiler.embeddable)
}
