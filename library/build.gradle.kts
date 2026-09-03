/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.vanniktech.maven.publish.AndroidSingleVariantLibrary

plugins {
    id("com.android.library")
    id("com.cmgapps.publish")
}

android {
    namespace = "com.cmgapps.logtag"
    compileSdk = 37
    defaultConfig {
        minSdk = 15
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    api(project(":annotation"))
    lintPublish(project(":linter"))
}

mavenPublishing {
    configure(
        AndroidSingleVariantLibrary(
            variant = "release",
        ),
    )
}
