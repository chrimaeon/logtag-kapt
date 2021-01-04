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

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

fun RepositoryHandler.helixTeamHubRepo(project: Project): ArtifactRepository {
    return maven {
        credentials {
            if (project.hasProperty("DEVEO_USERNAME")) {
                username = project.property("DEVEO_USERNAME") as String
                password = project.property("DEVEO_PASSWORD") as String
            } else {
                username = System.getenv("DEVEO_USERNAME")
                password = System.getenv("DEVEO_PASSWORD")
            }
        }
        url =
            project.uri("https://helixteamhub.cloud/cmgapps/projects/cmgapp-libs/repositories/maven/libraries")
    }
}

fun isNotCi(): Boolean {
    return System.getenv("CI") == null
}

fun isCi() = isNotCi().not()

fun PluginDependenciesSpec.cmgapps(module: String): PluginDependencySpec =
    id("com.cmgapps.gradle.$module")

val PluginDependenciesSpec.ktlint: PluginDependencySpec
    get() = cmgapps("ktlint")

