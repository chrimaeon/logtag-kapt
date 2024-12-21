/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 */

@file:Suppress("UnstableApiUsage")

rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(
    ":convention",
    ":plugin",
)
