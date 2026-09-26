/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Properties

pluginManagement {
    repositories {
        mavenCentral()
        google()

        maven {
            url =
                rootDir.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile
                    .resolve("build/local-staging")
                    .toURI()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven {
            url =
                rootDir.parentFile.parentFile.parentFile.parentFile.parentFile.parentFile
                    .resolve("build/local-staging")
                    .toURI()
        }
    }
}
