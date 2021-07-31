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

package com.cmgapps.kotlin

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class LogTagProcessorProviderShould {

    @Test
    fun `generate extension for kotlin class`() {
        val compilation = kotlin(
            "class.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag
              class TestClass
            """
        ).compile()

        @Language("kotlin")
        val expected = """
          @file:Suppress(
            "SpellCheckingInspection",
            "RedundantVisibilityModifier",
            "unused"
          )

          package cmgapps.test

          import kotlin.String
          import kotlin.Suppress

          public val TestClass.LOG_TAG: String
            inline get() = "TestClass"

        """.trimIndent()
        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestClassLogTag.kt" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `not generate for private class`() {
        val compilation = kotlin(
            "object.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag
              private class TestClass
            """
        ).compile()

        assertThat(compilation.result.exitCode, `is`(KotlinCompilation.ExitCode.COMPILATION_ERROR))
    }

    @Test
    fun `generate extension for object`() {
        val compilation = kotlin(
            "file1.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag
              object TestObject
            """
        ).compile()

        @Language("kotlin")
        val expected = """
          @file:Suppress(
            "SpellCheckingInspection",
            "RedundantVisibilityModifier",
            "unused"
          )

          package cmgapps.test

          import kotlin.String
          import kotlin.Suppress

          public val TestObject.LOG_TAG: String
            inline get() = "TestObject"

        """.trimIndent()
        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestObjectLogTag.kt" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `generate class for java class`() {
        val className = "TestJava"
        val compilation = SourceFile.java(
            "$className.java",
            """
              package cmgapps.test;

              @com.cmgapps.LogTag
              public class $className{}
            """
        ).compile()

        @Language("Java")
        val expected = """
            package cmgapps.test;

            import java.lang.String;

            class TestJavaLogTag {
              static final String LOG_TAG = "TestJava";
            }

        """.trimIndent()

        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestJavaLogTag.java" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `use custom logtag for kotlin class`() {
        val compilation = kotlin(
            "class.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag("MyCustomTag")
              class TestClass
            """
        ).compile()

        @Language("kotlin")
        val expected = """
          @file:Suppress(
            "SpellCheckingInspection",
            "RedundantVisibilityModifier",
            "unused"
          )

          package cmgapps.test

          import kotlin.String
          import kotlin.Suppress

          public val TestClass.LOG_TAG: String
            inline get() = "MyCustomTag"

        """.trimIndent()
        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestClassLogTag.kt" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `use custom tag for java class`() {
        val className = "TestJava"
        val compilation = SourceFile.java(
            "$className.java",
            """
              package cmgapps.test;

              @com.cmgapps.LogTag("MyCustomTag")
              public class $className{}
            """
        ).compile()

        @Language("Java")
        val expected = """
            package cmgapps.test;

            import java.lang.String;

            class TestJavaLogTag {
              static final String LOG_TAG = "MyCustomTag";
            }

        """.trimIndent()

        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestJavaLogTag.java" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `generate for internal class`() {
        val compilation = kotlin(
            "class.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag
              internal class TestClass
            """
        ).compile()

        @Language("kotlin")
        val expected = """
          @file:Suppress(
            "SpellCheckingInspection",
            "RedundantVisibilityModifier",
            "unused"
          )

          package cmgapps.test

          import kotlin.String
          import kotlin.Suppress

          internal val TestClass.LOG_TAG: String
            inline get() = "TestClass"

        """.trimIndent()
        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "TestClassLogTag.kt" }
                ?.readText(),
            `is`(expected)
        )
    }

    @Test
    fun `fail for not class-like declarations`() {
        val compilation = kotlin(
            "class.kt",
            """
              package cmgapps.test

              @com.cmgapps.LogTag
              fun test() {}
            """
        ).compile()

        val warning =
            "w: [ksp] ${compilation.kotlinCompilation.workingDir}/sources/class.kt:4: @LogTag can only be applied to Jetpack Compose @Composable functions"
        assertThat(compilation.result.messages, containsString(warning))
    }

    @Test
    fun `create composable TAG`() {
        val compilation = listOf(
            kotlin(
                "class.kt",
                """
                package cmgapps.test

                @com.cmgapps.LogTag
                @androidx.compose.runtime.Composable
                fun Test() {}
                """
            ),
            kotlin(
                "composable.kt",
                """
                package androidx.compose.runtime

                annotation class Composable
                """.trimIndent()
            )
        ).compile()

        @Language("kotlin")
        val expected = """
        @file:Suppress(
          "SpellCheckingInspection",
          "RedundantVisibilityModifier",
          "unused"
        )

        package cmgapps.test

        import kotlin.String
        import kotlin.Suppress

        public class ComposableTest {
          public companion object {
            public const val LOG_TAG: String = "Test"
          }
        }

        """.trimIndent()

        assertThat(
            compilation.kotlinCompilation.kspSourcesDir.walkTopDown().find { it.name == "ComposableTest.kt" }
                ?.readText(),
            `is`(expected)
        )
    }
}

private class PreparedCompilation(val kotlinCompilation: KotlinCompilation) {
    val result: KotlinCompilation.Result = kotlinCompilation.compile()
}

private fun SourceFile.compile() = listOf(this).compile()

private fun List<SourceFile>.compile() = PreparedCompilation(
    KotlinCompilation()
        .apply {
            inheritClassPath = true
            symbolProcessorProviders = listOf(LogTagProcessorProvider())
            sources = this@compile
        }
)
