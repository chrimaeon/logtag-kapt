/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.stream.Stream

val fixturesDir = File("src/functionalTest/fixtures")

fun Stream<Arguments>.cartesianProduct(other: Stream<Arguments>): Stream<Arguments> {
    val otherArguments = other.toList()
    return flatMap { s1 ->
        otherArguments.stream().map { s2 ->
            Arguments.of(*s1.get(), *s2.get())
        }
    }
}

private val gradleDir = File("gradle")

fun createBuildRunner(
    fixtureDir: File,
    vararg args: String = arrayOf("clean", "build"),
    kotlinVersion: String = MAX_KOTLIN_VERSION,
): GradleRunner {
    fixtureDir.resolve(gradleDir).apply {
        if (!exists()) mkdir()
        resolve("libs.versions.toml").writeText(
            """
            [versions]
            kotlin = "$kotlinVersion"
            logtag = "$LIBRARY_VERSION"
            """.trimIndent(),
        )
    }

    return GradleRunner
        .create()
        .withDebug(true)
        .withArguments(
            *args,
            "--info",
            "--stacktrace",
            "--continue",
        ).withProjectDir(fixtureDir)
        .forwardOutput()
}

fun assertExpectedFiles(
    fixtureDir: File,
    taskName: String = "",
) {
    val expectedDir = File(fixtureDir, "expected/$taskName")
    assertThat(expectedDir, anExistingDirectory())

    val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
    assertThat("$expectedDir is emtpy", expectedFiles, not(empty()))
    for (expectedFile in expectedFiles) {
        val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
        assertThat(
            actualFile,
            allOf(
                anExistingFile(),
                hasSameContentAs(expectedFile),
            ),
        )
    }
}

private fun hasSameContentAs(
    expected: File,
    charset: Charset = StandardCharsets.UTF_8,
    normalizeLineEndings: Boolean = true,
): Matcher<File> =
    object : TypeSafeDiagnosingMatcher<File>(File::class.java) {
        override fun describeTo(description: Description) {
            description
                .appendText("a file with same content as ")
                .appendValue(expected.path)
                .appendText(" (charset=")
                .appendValue(charset.name())
                .appendText(", normalizeLineEndings=")
                .appendValue(normalizeLineEndings)
                .appendText(")")
        }

        override fun matchesSafely(
            actual: File,
            mismatchDescription: Description,
        ): Boolean {
            val existingFileMatcher = anExistingFile()
            if (!existingFileMatcher.matches(expected)) {
                existingFileMatcher.describeMismatch(expected, mismatchDescription)
                return false
            }

            val expectedText = readText(expected, charset, normalizeLineEndings)
            val actualText = readText(actual, charset, normalizeLineEndings)

            if (expectedText == actualText) {
                return true
            }

            val expectedLines = expectedText.lines()
            val patch = DiffUtils.diff(expectedLines, actualText.lines())
            val unifiedDiff =
                UnifiedDiffUtils.generateUnifiedDiff(expected.path, actual.path, expectedLines, patch, 0)

            mismatchDescription
                .appendText("content differed:\n")

            unifiedDiff.forEach { line: String? -> mismatchDescription.appendText("$line\n") }

            return false
        }

        private fun readText(
            file: File,
            charset: Charset,
            normalizeLineEndings: Boolean,
        ): String {
            val text = file.readText(charset)
            return if (normalizeLineEndings) text.replace("\r\n", "\n") else text
        }
    }
