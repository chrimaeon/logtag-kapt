/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.cmgapps.logtag.LogTagPluginNames
import com.cmgapps.logtag.gradle.BuildConfig
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import javax.inject.Inject

abstract class LogTagExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        val enabled: Provider<Boolean> = objects.property<Boolean>().convention(true)
    }

@Suppress("unused")
class LogTagGradleSupportPlugin : KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> =
        with(kotlinCompilation.target.project) {
            val extension = extensions.getByType<LogTagExtension>()

            dependencies {
                add(
                    kotlinCompilation.implementationConfigurationName,
                    "${LogTagPluginNames.GROUP_ID}:annotation:${BuildConfig.LIBRARY_VERSION}",
                )
            }

            provider {
                mutableListOf(
                    SubpluginOption(
                        LogTagPluginNames.ENABLED_OPTION_NAME,
                        lazy { extension.enabled.getOrElse(true).toString() },
                    ),
                )
            }
        }

    override fun getCompilerPluginId(): String = LogTagPluginNames.PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = LogTagPluginNames.GROUP_ID,
            artifactId = "logtag-compiler-plugin-cli",
            version = BuildConfig.LIBRARY_VERSION,
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun apply(target: Project) {
        with(target) {
            extensions.create("logTag", LogTagExtension::class.java)
        }
    }
}
