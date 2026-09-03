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
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@Suppress("kotlin:S6526")
public abstract class LogTagExtension {
    public abstract val enabled: Property<Boolean>

    init {
        enabled.convention(true)
    }
}

@Suppress("unused")
public class LogTagGradleSupportPlugin : KotlinCompilerPluginSupportPlugin {
    private lateinit var kotlinVersion: Property<String>

    @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) {
        with(target) {
            kotlinVersion = extensions.getByType(KotlinBaseExtension::class.java).compilerVersion
            extensions.create("logTag", LogTagExtension::class.java)

            // TODO: check if android project and apply the linter library
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

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
            version = "${kotlinVersion.get()}-${BuildConfig.LIBRARY_VERSION}",
        )
}
