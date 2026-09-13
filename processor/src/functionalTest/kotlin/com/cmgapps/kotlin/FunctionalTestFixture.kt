/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.kotlin

import com.tschuchort.compiletesting.SourceFile
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.relativeTo

internal class FunctionalTestFixture private constructor(
    internal val sources: List<SourceFile>,
    private val expectedGeneratedSources: Map<String, String>,
) {
    internal fun assertGeneratedSources(generatedSourcesDir: File) {
        val actualGeneratedSources = generatedSourcesDir.toPath().readFilesRecursively()

        assertThat(actualGeneratedSources.keys, `is`(expectedGeneratedSources.keys))
        expectedGeneratedSources.forEach { (path, expectedContent) ->
            assertThat(actualGeneratedSources.getValue(path), `is`(expectedContent))
        }
    }

    internal companion object {
        fun load(id: String): FunctionalTestFixture {
            require(id.isNotBlank()) { "Fixture id must not be blank." }
            require(!id.contains("..")) { "Fixture id must not contain '..': $id" }

            val fixtureRoot =
                checkNotNull(FunctionalTestFixture::class.java.classLoader.getResource("test/$id")) {
                    "Fixture resource not found: test/$id"
                }.let { Paths.get(it.toURI()) }
            val expectedRoot = fixtureRoot.resolve(EXPECTED_DIRECTORY)
            check(Files.isDirectory(expectedRoot)) {
                "Fixture test/$id must contain an $EXPECTED_DIRECTORY directory."
            }

            val sources =
                fixtureRoot
                    .readFilesRecursively()
                    .filterKeys { path -> !path.startsWith("$EXPECTED_DIRECTORY/") }
                    .map { (path, content) -> path.toSourceFile(content) }
            check(sources.isNotEmpty()) { "Fixture test/$id does not contain any source files." }

            return FunctionalTestFixture(sources, expectedRoot.readFilesRecursively())
        }

        private const val EXPECTED_DIRECTORY = "expected"
    }
}

internal fun functionalTestFixture(id: String): FunctionalTestFixture = FunctionalTestFixture.load(id)

private fun Path.readFilesRecursively(): Map<String, String> =
    Files
        .walk(this)
        .use { paths ->
            paths
                .iterator()
                .asSequence()
                .filter(Path::isRegularFile)
                .sortedBy(Path::toString)
                .associate { path ->
                    path.relativeTo(this).toFixturePath() to path.readText(UTF_8).normalizeLineEndings()
                }
        }

private fun String.toSourceFile(content: String): SourceFile =
    when (substringAfterLast('.', missingDelimiterValue = "")) {
        "kt" -> SourceFile.kotlin(this, content)
        "java" -> SourceFile.java(this, content)
        else -> error("Unsupported fixture source file extension: $this")
    }

private fun Path.toFixturePath(): String = toString().replace(File.separatorChar, '/')

private fun String.normalizeLineEndings(): String = replace("\r\n", "\n").replace('\r', '\n')
