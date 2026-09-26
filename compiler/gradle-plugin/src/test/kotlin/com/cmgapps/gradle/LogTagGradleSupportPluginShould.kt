/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package com.cmgapps.gradle

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.TestExtension
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.problems.ProblemReporter
import org.gradle.api.problems.Problems
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.samePropertyValuesAs
import org.hamcrest.beans.HasPropertyWithValue.hasProperty
import org.jetbrains.kotlin.buildtools.api.ExperimentalBuildToolsApi
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.nio.file.Path

class LogTagGradleSupportPluginShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var project: Project
    private lateinit var problems: Problems
    private lateinit var problemsReporter: ProblemReporter

    @BeforeEach
    fun setUp() {
        problemsReporter = mock()
        problems =
            mock {
                on { reporter } doReturn problemsReporter
            }

        project =
            ProjectBuilder
                .builder()
                .withProjectDir(testProjectDir.toFile())
                .build()
    }

    @Test
    fun `set plugin id`() {
        assertThat(LogTagGradleSupportPlugin(problems).getCompilerPluginId(), `is`(BuildConfig.KOTLIN_PLUGIN_ID))
    }

    @Test
    fun `get plugin artifact`() {
        assertThat(
            LogTagGradleSupportPlugin(problems).getPluginArtifact(),
            (
                samePropertyValuesAs(
                    SubpluginArtifact(
                        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
                        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
                        version = BuildConfig.LIBRARY_VERSION,
                    ),
                )
            ),
        )
    }

    @Test
    fun `apply plugin`() {
        project.plugins.apply("kotlin")

        LogTagGradleSupportPlugin(problems).apply(project)

        assertThat(project.extensions.findByType(LogTagExtension::class.java), notNullValue())
    }

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - {0}")
    @MethodSource("androidPluginIds")
    fun `add lint dependency for each android plugin`(pluginId: String) {
        project.plugins.apply(pluginId)
        val minSdk = 26

        project.extensions.configure(CommonExtension::class.java) {
            it.namespace = "com.example"
            it.compileSdk = 36
            it.defaultConfig.minSdk = minSdk
        }

        if (pluginId == "com.android.test") {
            project.extensions.configure(TestExtension::class.java) {
                it.targetProjectPath = ":"
            }
        }

        LogTagGradleSupportPlugin(problems).apply(project)

        (project as ProjectInternal).evaluate()

        assertThat(
            project.configurations
                .getByName("implementation")
                .dependencies
                .map { it.name },
            containsInAnyOrder("android-lint"),
        )
    }

    @Test
    fun `add lint dependency for android multiplatform target`() {
        applyKotlinAndroidMultiplatformPlugin()

        (project as ProjectInternal).evaluate()

        assertThat(
            project.configurations
                .getByName("androidMainImplementation")
                .dependencies
                .map { it.name },
            containsInAnyOrder("android-lint"),
        )
    }

    @Test
    fun `set min sdk for android multiplatform target`() {
        val minSdk = 24
        val androidExtension = applyKotlinAndroidMultiplatformPlugin()
        androidExtension.minSdk = minSdk

        (project as ProjectInternal).evaluate()

        val logTagExtension = project.extensions.getByType(LogTagExtension::class.java) as LogTagExtensionInternal
        assertThat(logTagExtension.minSdk.get(), `is`(minSdk))
    }

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - {0}")
    @MethodSource("androidPluginIds")
    fun `set min sdk for each android plugin`(pluginId: String) {
        project.plugins.apply(pluginId)
        val minSdk = 24

        project.extensions.configure(CommonExtension::class.java) {
            it.namespace = "com.example"
            it.compileSdk = 36
            it.defaultConfig.minSdk = minSdk
        }

        if (pluginId == "com.android.test") {
            project.extensions.configure(TestExtension::class.java) {
                it.targetProjectPath = ":"
            }
        }

        LogTagGradleSupportPlugin(problems).apply(project)

        (project as ProjectInternal).evaluate()

        val logTagExtension = project.extensions.getByType(LogTagExtension::class.java) as LogTagExtensionInternal
        assertThat(
            logTagExtension.minSdk.get(),
            `is`(minSdk),
        )
    }

    @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
    @Test
    fun `report problems when less than min version`() {
        project.plugins.apply("kotlin")

        project.kotlinExtension.compilerVersion.set("2.0.0")
        LogTagGradleSupportPlugin(problems).apply(project)

        (project as ProjectInternal).evaluate()

        assertThat(project.extensions.findByType(LogTagExtension::class.java), notNullValue())
        verify(problemsReporter).report(any(), any())
    }

    @OptIn(ExperimentalBuildToolsApi::class, ExperimentalKotlinGradlePluginApi::class)
    @Test
    fun `report problems when greater than max version`() {
        project.plugins.apply("kotlin")
        project.kotlinExtension.compilerVersion.set("3.0.0")
        LogTagGradleSupportPlugin(problems).apply(project)

        (project as ProjectInternal).evaluate()

        assertThat(project.extensions.findByType(LogTagExtension::class.java), notNullValue())
        verify(problemsReporter).report(any(), any())
    }

    @Test
    fun `apply to compilation`() {
        project.plugins.apply("kotlin")

        val defaultSourceSet = mock<KotlinSourceSet>()
        val compileTaskProvider: TaskProvider<out KotlinCompilationTask<*>> = mock()
        val kotlinTarget =
            mock<KotlinTarget> {
                on { this.project } doReturn project
            }
        val compilation: KotlinCompilation<*> =
            mock {
                on { this.defaultSourceSet } doReturn defaultSourceSet
                on { this.compileTaskProvider } doReturn compileTaskProvider
                on { this.target } doReturn kotlinTarget
            }
        val result =
            LogTagGradleSupportPlugin(problems).run {
                apply(project)
                applyToCompilation(compilation)
            }

        assertThat(
            result.get(),
            containsInAnyOrder(
                hasProperty("key", `is`("enabled")),
                hasProperty("key", `is`("android.minSdk")),
            ),
        )
    }

    @Test
    fun `set applicable to true`() {
        val compilation: KotlinCompilation<*> = mock()
        assertThat(LogTagGradleSupportPlugin(problems).isApplicable(compilation), `is`(true))
    }

    companion object {
        @JvmStatic
        fun androidPluginIds() =
            ANDROID_IDS.filter {
                it != "com.android.feature" // deprecated old plugin }
            }
    }

    private fun applyKotlinAndroidMultiplatformPlugin(): KotlinMultiplatformAndroidLibraryExtension {
        project.plugins.apply("org.jetbrains.kotlin.multiplatform")
        project.plugins.apply("com.android.kotlin.multiplatform.library")

        val multiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        val androidExtension =
            multiplatformExtension.extensions.getByType(KotlinMultiplatformAndroidLibraryExtension::class.java)
        androidExtension.namespace = "com.example"
        androidExtension.compileSdk = 36

        LogTagGradleSupportPlugin(problems).apply(project)

        return androidExtension
    }
}
