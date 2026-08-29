/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        substitute(module("com.cmgapps.logtag:compiler-plugin:$libraryVersion"))
            .using(project(":compiler:compiler-plugin"))
            .because("Developers can see local changes reflected in the sample project")
        substitute(module("com.cmgapps.logtag:com.cmgapps.logtag.gradle.plugin:$libraryVersion"))
            .using(project(":compiler:gradle-plugin"))
            .because("Developers can see local changes reflected in the sample project")
    }
}

rootProject.name = "example-compiler-plugin"
