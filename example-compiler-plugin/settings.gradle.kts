/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Properties

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

private val rootGradleProps =
    Properties().apply {
        File("../gradle.properties").inputStream().use(::load)
    }

includeBuild("../.") {
    val libraryVersion = rootGradleProps.getProperty("versionName")
    logger.lifecycle("Replacing LogTag module dependencies with local projects")
    dependencySubstitution {
        substitute(module("com.cmgapps.logtag:annotation:$libraryVersion"))
            .using(project(":annotation"))
            .because("Developers can see local changes reflected in the sample project")
        substitute(module("com.cmgapps.logtag:compiler-plugin"))
            .using(project(":compiler:compiler-plugin"))
            .because("Developers can see local changes reflected in the sample project")
        substitute(module("com.cmgapps.logtag:com.cmgapps.logtag.gradle.plugin:$libraryVersion"))
            .using(project(":compiler:gradle-plugin"))
            .because("Developers can see local changes reflected in the sample project")
    }
}

rootProject.name = "example-compiler-plugin"
