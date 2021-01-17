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

package com.cmgapps.gradle

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("unused")
open class LogTagPlugin : KotlinCompilerPluginSupportPlugin {
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) =
        kotlinCompilation.target.project.plugins.hasPlugin(LogTagPlugin::class.java)

    override fun getCompilerPluginId() = "com.cmgapps.logtag.compiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = GROUP,
        artifactId = "compiler",
        version = VERSION
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        with(kotlinCompilation.target.project) {
            val srcGenDir =
                buildDir.resolve("generated").resolve("source").resolve("logtag").resolve(kotlinCompilation.name)

            val sourceSets =
                (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer
            sourceSets.getOrCreate("main").java {
                it.srcDirs(srcGenDir)
            }

            provider {
                listOf(
                    FilesSubpluginOption(
                        key = "src-gen-dir",
                        files = listOf(srcGenDir)
                    )
                )
            }
        }

    private fun SourceSetContainer.getOrCreate(name: String): SourceSet {
        return findByName(name) ?: create(name)
    }
}