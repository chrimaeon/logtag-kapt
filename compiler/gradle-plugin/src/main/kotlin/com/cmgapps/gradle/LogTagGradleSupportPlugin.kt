/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.cmgapps.logtag.gradle.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

abstract class LogTagExtension {
    abstract val enabled: Property<Boolean>

    init {
        enabled.convention(true)
    }
}

@Suppress("unused")
class LogTagGradleSupportPlugin : KotlinCompilerPluginSupportPlugin {
    lateinit var kotlinVersion: String

    override fun apply(target: Project) {
        with(target) {
            kotlinVersion = getKotlinPluginVersion()
            extensions.create("logTag", LogTagExtension::class.java)
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        kotlinCompilation.defaultSourceSet.dependencies {
            implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
        }

        return with(kotlinCompilation.target.project) {
            val extension = extensions.getByType(LogTagExtension::class.java)
            objects.listProperty(SubpluginOption::class.java).apply {
                add(
                    extension.enabled.map { SubpluginOption("enabled", it.toString()) },
                )
            }
        }
    }

    override fun getCompilerPluginId(): String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
            artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
            version = "$kotlinVersion-${BuildConfig.LIBRARY_VERSION}",
        )
}
