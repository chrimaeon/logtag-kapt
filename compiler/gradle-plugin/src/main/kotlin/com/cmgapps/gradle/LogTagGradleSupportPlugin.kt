/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.cmgapps.logtag.gradle.BuildConfig
import org.apache.maven.artifact.versioning.ComparableVersion
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
            version = BuildConfig.LIBRARY_VERSION,
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val project = kotlinCompilation.target.project
        val requiredKotlinVersion = ComparableVersion(BuildConfig.KOTLIN_VERSION)
        val kotlinVersion = ComparableVersion(project.getKotlinPluginVersion())

        if (requiredKotlinVersion < kotlinVersion) {
            project.logger.warn(
                "logtag-${BuildConfig.LIBRARY_VERSION} is too old for kotlin-$kotlinVersion. " +
                    "Please upgrade logtag or downgrade kotlin-gradle-plugin to $requiredKotlinVersion.",
            )
        }
        if (requiredKotlinVersion > kotlinVersion) {
            project.logger.warn(
                "logtag-${BuildConfig.LIBRARY_VERSION} is too new for kotlin-$kotlinVersion. " +
                    "Please upgrade kotlin-gradle-plugin to $requiredKotlinVersion.",
            )
        }

        return true
    }

    override fun apply(target: Project) {
        with(target) {
            extensions.create("logTag", LogTagExtension::class.java)
        }
    }
}
