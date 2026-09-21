/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.api.plugins.jvm.JvmTestSuite

plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin.get()
    id("com.cmgapps.logtag") version libs.versions.logtag.get()
    id("jvm-test-suite")
}

testing.suites {
    named("test", JvmTestSuite::class) {
        useJUnit()
    }
}
