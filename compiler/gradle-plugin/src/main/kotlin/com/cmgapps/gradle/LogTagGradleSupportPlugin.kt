/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Project
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Provider
import org.jetbrains.annotations.VisibleForTesting
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.kotlinToolingVersion
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion
import javax.inject.Inject

@VisibleForTesting
internal val ANDROID_IDS =
    listOf(
        "com.android.application",
        "com.android.library",
        "com.android.feature",
        "com.android.dynamic-feature",
        "com.android.test",
    )

@Suppress("unused", "UnstableApiUsage")
public class LogTagGradleSupportPlugin
    @Inject
    constructor(
        problems: Problems,
    ) : KotlinCompilerPluginSupportPlugin {
        private val reporter = problems.reporter

        @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
        override fun apply(target: Project) {
            with(target) {
                val compilerVersionProvider =
                    target.kotlinExtension.compilerVersion
                        .map { KotlinToolingVersion(it) }
                        .orElse(target.provider { target.kotlinToolingVersion })

                val logTagExtension = objects.newInstance(LogTagExtensionInternal::class.java)
                logTagExtension.minSdk.convention(Int.MAX_VALUE)
                extensions.add(LogTagExtension::class.java, "logTag", logTagExtension)

                ANDROID_IDS.forEach {
                    plugins.withId(it) {
                        logger.info("Adding Android Lint library dependency")
                        dependencies.add("implementation", BuildConfig.ANDROID_LINT_LIBRARY_COORDINATES)

                        val commonAndroidExtension = extensions.getByType(CommonExtension::class.java)
                        afterEvaluate {
                            val minSdk = commonAndroidExtension.defaultConfig.minSdk
                            logger.info("Setting LogTag minSdk to Android minSdk {}", minSdk)
                            logTagExtension.minSdk.set(minSdk)
                        }
                    }
                }

                plugins.withId("com.android.kotlin.multiplatform.library") {
                    target.plugins.apply("com.android.lint")

                    val multiplatformExtension = extensions.getByType(KotlinMultiplatformExtension::class.java)

                    multiplatformExtension.targets.configureEach { target ->
                        val compilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                        if (compilation.platformType == KotlinPlatformType.androidJvm) {
                            compilation.defaultSourceSet.dependencies {
                                logger.info("Adding Android Lint library dependency")
                                implementation(BuildConfig.ANDROID_LINT_LIBRARY_COORDINATES)
                            }
                        }
                    }

                    multiplatformExtension.extensions
                        .findByType(KotlinMultiplatformAndroidLibraryExtension::class.java)
                        ?.let { androidExtension ->
                            afterEvaluate {
                                val minSdk = androidExtension.minSdk
                                logger.info("Setting LogTag minSdk to Android minSdk {}", minSdk)
                                logTagExtension.minSdk.set(minSdk)
                            }
                        }
                }

                afterEvaluate {
                    val compilerVersion = compilerVersionProvider.get()

                    project.logger.lifecycle("Project initialized")

                    val minSupported = KotlinToolingVersion(BuildConfig.MIN_KOTLIN_VERSION)
                    val maxSupported = KotlinToolingVersion(BuildConfig.MAX_KOTLIN_VERSION)
                    val isSupported = compilerVersion in minSupported..maxSupported

                    if (!isSupported) {
                        val label: String
                        val details =
                            "Supported Kotlin versions: ${BuildConfig.MIN_KOTLIN_VERSION} - ${BuildConfig.MAX_KOTLIN_VERSION}"
                        val solution: String
                        val problemId: ProblemId

                        if (compilerVersion < minSupported) {
                            label =
                                "LogTag '${BuildConfig.LIBRARY_VERSION}' requires Kotlin ${BuildConfig.MIN_KOTLIN_VERSION} or later, but this build uses '$compilerVersion'"
                            solution =
                                "Please upgrade Kotlin to at least '${BuildConfig.MIN_KOTLIN_VERSION}'"
                            problemId =
                                ProblemId.create(
                                    "kotlin-version-too-old",
                                    "Kotlin version is too old for LogTag",
                                    PROBLEM_GROUP,
                                )
                        } else {
                            label = "This build uses unrecognized Kotlin version '$compilerVersion'"
                            solution =
                                "If you have any issues, please upgrade LogTag (if applicable) or use a supported Kotlin version."
                            problemId =
                                ProblemId.create(
                                    "kotlin-version-unrecognized",
                                    "Kotlin version is unrecognized by LogTag",
                                    PROBLEM_GROUP,
                                )
                        }

                        reporter.report(problemId) { spec ->
                            spec
                                .contextualLabel(label)
                                .details(details)
                                .solution(solution)
                        }

                        logger.warn("$label $details\n$solution")
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

        private companion object {
            val PROBLEM_GROUP: ProblemGroup = ProblemGroup.create("logtag-group", "LogTag Problems")
        }
    }
