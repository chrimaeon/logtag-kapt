/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.application")
    id("com.cmgapps.logtag") version libs.versions.logtag.get()
}

android {
    namespace = "com.cmgapps.test"
    compileSdk = 36
    defaultConfig {
        minSdk = 28
    }
}
