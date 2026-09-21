/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.gradle

import com.cmgapps.logtag.gradle.BuildConfig.LIBRARY_VERSION
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

class PluginShould {
    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0} Kotlin Version = {1}")
    @MethodSource("versions")
    fun `apply to various gradle and kotlin version combinations`(
        gradleVersion: String,
        kotlinVersion: String,
    ) {
        val result =
            createBuildRunner(
                File(fixturesDir, "apply-plugin"),
                kotlinVersion = kotlinVersion,
            ).apply {
                if (gradleVersion != LATEST_VERSION) {
                    withGradleVersion(gradleVersion)
                }
            }.build()

        assertThat(
            "Gradle version $gradleVersion and Kotlin version $kotlinVersion",
            result.task(":assemble")?.outcome,
            `is`(TaskOutcome.SUCCESS),
        )
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0} Kotlin Version = {1}")
    @MethodSource("versions")
    fun `handle class annotation`(
        gradleVersion: String,
        kotlinVersion: String,
    ) {
        val result =
            createBuildRunner(
                File(fixturesDir, "class-annotation"),
                kotlinVersion = kotlinVersion,
            ).apply {
                if (gradleVersion != LATEST_VERSION) {
                    withGradleVersion(gradleVersion)
                }
            }.build()

        assertThat(
            "Gradle version $gradleVersion and Kotlin version $kotlinVersion",
            result.task(":assemble")?.outcome,
            `is`(TaskOutcome.SUCCESS),
        )
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0} Kotlin Version = {1}")
    @MethodSource("versions")
    fun `handle function annotation`(
        gradleVersion: String,
        kotlinVersion: String,
    ) {
        val result =
            createBuildRunner(
                File(fixturesDir, "function-annotation"),
                kotlinVersion = kotlinVersion,
            ).apply {
                if (gradleVersion != LATEST_VERSION) {
                    withGradleVersion(gradleVersion)
                }
            }.build()

        assertThat(
            "Gradle version $gradleVersion and Kotlin version $kotlinVersion",
            result.task(":assemble")?.outcome,
            `is`(TaskOutcome.SUCCESS),
        )
    }

    @Test
    fun `apply android lint library on Android`() {
        val result =
            createBuildRunner(
                File(fixturesDir, "apply-android-lint-dependency-android"),
                args = arrayOf("clean", "build", ":app:dependencies"),
            ).build()

        assertThat(result.output, containsString("--- com.cmgapps.logtag:android-lint:${LIBRARY_VERSION}"))
    }

    @Test
    fun `apply android lint library on KMP`() {
        val result =
            createBuildRunner(
                File(fixturesDir, "apply-android-lint-dependency-kmp"),
                args = arrayOf("clean", "build", ":lib:dependencies"),
            ).build()

        assertThat(result.output, containsString("--- com.cmgapps.logtag:android-lint:${LIBRARY_VERSION}"))
    }

    @Test
    fun `report when log tag is too long on Android`() {
        val fixturesDir = File(fixturesDir, "report-too-long-log-tag-on-android")
        val result =
            createBuildRunner(
                fixturesDir,
                args = arrayOf("clean", "build", ":app:lint"),
            ).build()

        assertExpectedFiles(fixturesDir, "lint")
    }

    @Test
    fun `report when log tag is too long on KMP`() {
        val fixturesDir = File(fixturesDir, "report-too-long-log-tag-on-kmp")
        val result =
            createBuildRunner(
                fixturesDir,
                args = arrayOf("clean", "build", ":lib:lint"),
            ).build()

        assertExpectedFiles(fixturesDir, "lint")
    }

    companion object {
        @JvmStatic
        fun versions() = gradleVersions().cartesianProduct(kotlinVersions())

        @JvmStatic
        fun gradleVersions(): Stream<Arguments> =
            buildList {
                add(MINIMUM_GRADLE_VERSION)
                add(LATEST_VERSION)
                if (System.getenv("CI") == null) {
                    add("9.2.0")
                    add("9.3.0")
                    add("9.4.0")
                    add("9.5.0")
                    add("9.6.0")
                }
            }.stream().map { arguments(it) }

        @JvmStatic
        fun kotlinVersions(): Stream<Arguments> =
            buildList {
                add(BuildConfig.MIN_KOTLIN_VERSION)
                add(BuildConfig.MAX_KOTLIN_VERSION)
                if (System.getenv("CI") == null) {
                    add("2.3.20")
                    add("2.4.0")
                }
            }.stream().map { arguments(it) }
    }
}

private const val LATEST_VERSION = "latest"
