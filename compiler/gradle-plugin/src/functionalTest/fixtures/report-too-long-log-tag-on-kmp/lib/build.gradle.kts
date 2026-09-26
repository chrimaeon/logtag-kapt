/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.cmgapps.logtag") version libs.versions.logtag.get()
}

kotlin {
    jvm()

    lint {
        absolutePaths = false
        htmlReport = false // containes timestamp
        sarifReport = false // containes abs paths
        textReport = true
        xmlReport = true
    }

    android {
        namespace = "com.cmgapps.test"
        compileSdk = 36
        minSdk = 24

        lint {
            absolutePaths = false
            htmlReport = false // containes timestamp
            sarifReport = false // containes abs paths
            textReport = true
            xmlReport = true
        }
    }
}
