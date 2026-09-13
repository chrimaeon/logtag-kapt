/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalCompilerApi::class)

package com.cmgapps.kotlin

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LogTagProcessorProviderShould {
    @ParameterizedTest(
        name = "{0}",
    )
    @ValueSource(
        strings = [
            "generate_extension_for_kotlin_class",
            "generate_extension_for_object",
            "generate_class_for_java_class",
            "use_custom_logtag_for_kotlin_class",
            "use_custom_tag_for_java_class",
            "create_composable_tag",
            "generate_for_internal_class",
        ],
    )
    fun `generate log tag for`(testId: String) {
        val fixture = functionalTestFixture(testId)
        val compilation = fixture.sources.compile()

        fixture.assertGeneratedSources(compilation.kotlinCompilation.kspSourcesDir)
    }

    @Test
    fun `not generate for private class`() {
        val compilation =
            kotlin(
                "private.kt",
                """
              package cmgapps.test

              @com.cmgapps.LogTag
              private class TestClass
            """,
            ).compile()

        assertThat(compilation.result.exitCode, `is`(KotlinCompilation.ExitCode.COMPILATION_ERROR))
    }

    @Test
    fun `fail for not class-like declarations`() {
        val compilation =
            kotlin(
                "class.kt",
                """
              package cmgapps.test

              @com.cmgapps.LogTag
              fun test() {}
            """,
            ).compile()

        val warning =
            "w: [ksp] ${compilation.kotlinCompilation.workingDir}/sources/class.kt:4: @LogTag can only be applied to Jetpack Compose @Composable functions"
        assertThat(compilation.result.messages, containsString(warning))
    }

    @Test
    fun `truncate log tags for older Android versions`() {
        val fixture = functionalTestFixture("truncate_log_tags_for_older_android_versions")
        val compilation = fixture.sources.compile(kspArgs = mapOf("logtag.androidMinSdkVersion" to "23"))

        fixture.assertGeneratedSources(compilation.kotlinCompilation.kspSourcesDir)
    }

    @Test
    fun `not truncate log tags for  Android versions greater or equal to 26`() {
        val fixture = functionalTestFixture("not_truncate_log_tags_for_android_versions_greater_or_equal_to_26")
        val compilation = fixture.sources.compile(kspArgs = mapOf("logtag.androidMinSdkVersion" to "26"))

        fixture.assertGeneratedSources(compilation.kotlinCompilation.kspSourcesDir)
    }
}

private class PreparedCompilation(
    val kotlinCompilation: KotlinCompilation,
) {
    val result: JvmCompilationResult = kotlinCompilation.compile()
}

private fun SourceFile.compile(kspArgs: Map<String, String> = emptyMap()) = listOf(this).compile(kspArgs)

private fun List<SourceFile>.compile(kspArgs: Map<String, String> = emptyMap()) =
    PreparedCompilation(
        KotlinCompilation()
            .apply {
                useKsp2()
                inheritClassPath = true
                symbolProcessorProviders += listOf(LogTagProcessorProvider())
                sources = this@compile
                kspProcessorOptions += kspArgs
            },
    )
