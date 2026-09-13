/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.cmgapps.logtag.gradle.BuildConfig
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

private val ANDROID_IDS =
    listOf(
        "com.android.application",
        "com.android.library",
        "com.android.feature",
        "com.android.dynamic-feature",
        "com.android.test",
    )

@Suppress("unused")
public class LogTagGradleSupportPlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        with(target) {
            val logTagExtension = objects.newInstance(LogTagExtensionInternal::class.java)
            logTagExtension.minSdk.convention(Int.MAX_VALUE)
            extensions.add(LogTagExtension::class.java, "logTag", logTagExtension)

            ANDROID_IDS.forEach {
                plugins.withId(it) {
                    dependencies.add("implementation", BuildConfig.ANDROID_LINT_LIBRARY_COORDINATES)

                    val commonAndroidExtension = extensions.getByType(CommonExtension::class.java)
                    afterEvaluate {
                        logTagExtension.minSdk.set(commonAndroidExtension.defaultConfig.minSdk)
                    }
                }
            }

            plugins.withId("com.android.kotlin.multiplatform.library") {
                val multiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)
                multiplatformExtension.sourceSets.named("androidMain") {
                    it.dependencies {
                        implementation(BuildConfig.ANDROID_LINT_LIBRARY_COORDINATES)
                    }
                }

                multiplatformExtension.extensions
                    .findByType(KotlinMultiplatformAndroidLibraryExtension::class.java)
                    ?.let { androidExtension ->
                        afterEvaluate {
                            logTagExtension.minSdk.set(androidExtension.minSdk)
                        }
                    }
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        kotlinCompilation.defaultSourceSet.dependencies {
            implementation(BuildConfig.ANNOTATIONS_LIBRARY_COORDINATES)
        }

        kotlinCompilation.compileTaskProvider.configure {
            // Run this compiler plugin before Compose plugin.
            it.compilerOptions.freeCompilerArgs.add(
                "-Xcompiler-plugin-order=${BuildConfig.KOTLIN_PLUGIN_ID}>androidx.compose.compiler.plugins.kotlin",
            )
        }

        return with(kotlinCompilation.target.project) {
            val extension = extensions.getByType(LogTagExtension::class.java) as LogTagExtensionInternal
            objects.listProperty(SubpluginOption::class.java).apply {
                add(extension.enabled.map { SubpluginOption("enabled", it.toString()) })
                add(extension.minSdk.map { SubpluginOption("android.minSdk", it.toString()) })
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
}
