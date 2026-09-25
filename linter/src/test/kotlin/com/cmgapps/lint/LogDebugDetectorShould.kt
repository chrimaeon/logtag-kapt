/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestMode
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.jupiter.api.Test

class LogDebugDetectorShould : LintDetectorTest() {
    private val timberStub =
        java(
            """
                package timber.log;
                public class Timber {
                    private Timber() {}

                    public static void d(String message, Object... args) {}
                    public static void v(String message, Object... args) {}
                    public static void e(String message, Object... args) {}
                    public static Tree tag(String tag) {}

                    public static class Tree {
                        public void d(String message, Object... args) {}
                        public void v(String message, Object... args) {}
                        public void e(String message, Object... args) {}
                    }
                }
            """,
        ).indented()

    private val manifestStub = manifest("<manifest package=\"com.cmgapps\"/>")

    @Test
    fun `report missing if statement in java class`() {
        lint()
            .files(
                manifestStub,
                java(
                    """
                    import android.util.Log;
                    public class Test {
                        public void test() {
                           Log.d(TAG, "Message");
                        }
                    }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Log.d(TAG, "Message");
                       ~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -1,0 +2 @@
                +import com.cmgapps.BuildConfig;
                @@ -4 +5,4 @@
                -       Log.d(TAG, "Message");
                +       if (BuildConfig.DEBUG) {
                +    Log.d(TAG, "Message");
                +}
                +;
                Autofix for src/Test.java line 4: Surround with `if (Log.isLoggable(...))`:
                @@ -4 +4,4 @@
                -       Log.d(TAG, "Message");
                +       if (Log.isLoggable(TAG, Log.DEBUG)) {
                +    Log.d(TAG, "Message");
                +}
                +;
                """.trimIndent(),
            )
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in java class`() {
        lint()
            .files(
                java(
                    """
                    public class Test {
                       public void test() {
                           if (BuildConfig.DEBUG) {
                               android.util.Log.d("TestTag", "Message");
                           }
                       }
                    }
                """,
                ).indented(),
            ).run()
            .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log#isLoggable in java class`() {
        lint()
            .files(
                java(
                    """
                    public class Test {
                       public void test() {
                           if (android.util.Log.isLoggable("TestTag", Log.DEBUG)) {
                               android.util.Log.d("TestTag", "Message");
                           }
                       }
                    }
                """,
                ).indented(),
            ).run()
            .expect("No warnings.")
    }

    @Test
    fun `report missing if statement in kotlin class`() {
        lint()
            .files(
                manifestStub,
                kotlin(
                    """
                class Test {
                    fun test() {
                        android.util.Log.v("TestTag", "Message")
                    }
                }
                """,
                ).indented(),
            ).skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect(
                """
                src/Test.kt:3: Warning: The log call Log.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                        android.util.Log.v("TestTag", "Message")
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.kt line 3: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1,2 @@
                +import android.util.Log
                +import com.cmgapps.BuildConfig
                @@ -3 +5,4 @@
                -        android.util.Log.v("TestTag", "Message")
                +        if (BuildConfig.DEBUG) {
                +    Log.v("TestTag", "Message")
                +}
                +
                Autofix for src/Test.kt line 3: Surround with `if (Log.isLoggable(...))`:
                @@ -0,0 +1 @@
                +import android.util.Log
                @@ -3 +4,4 @@
                -        android.util.Log.v("TestTag", "Message")
                +        if (Log.isLoggable("TestTag", Log.VERBOSE)) {
                +    Log.v("TestTag", "Message")
                +}
                +
                """.trimIndent(),
            )
    }

    @Test
    fun `report no errors if nested in BuildConfig DEBUG in kotlin class`() {
        lint()
            .files(
                kotlin(
                    """
                class Test {
                   fun test() {
                       if (BuildConfig.DEBUG) {
                           android.util.Log.v("TestTag", "Message")
                       }
                   }
                }""",
                ).indented(),
            ).skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report no errors if nested in Log isLoggable in kotlin class`() {
        lint()
            .files(
                kotlin(
                    """
                class Test {
                   fun test() {
                       if (android.util.Log.isLoggable("TestTag", Log.DEBUG)) {
                           android.util.Log.d("TestTag", "Message")
                       }
                   }
                }""",
                ).indented(),
            ).skipTestModes(TestMode.IF_TO_WHEN)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun `report errors if Timber in kotlin class`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                kotlin(
                    """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.d("Message")
                   }
                }""",
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.d("Message")
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            ).expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig
                @@ -4 +5,4 @@
                -       Timber.d("Message")
                +       if (BuildConfig.DEBUG) {
                +    Timber.d("Message")
                +}
                +
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in java class`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                java(
                    """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.d("Message");
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.d("Message");
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig;
                @@ -4 +5,4 @@
                -       Timber.d("Message");
                +       if (BuildConfig.DEBUG) {
                +    Timber.d("Message");
                +}
                +;
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                java(
                    """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.v("Message");
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.v("Message");
                       ~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """,
            ).expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig;
                @@ -4 +5,4 @@
                -       Timber.v("Message");
                +       if (BuildConfig.DEBUG) {
                +    Timber.v("Message");
                +}
                +;
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose and tag`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                java(
                    """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.tag("TestTag").v("Message");
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag").v("Message");
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig;
                @@ -4 +5,4 @@
                -       Timber.tag("TestTag").v("Message");
                +       if (BuildConfig.DEBUG) {
                +    Timber.tag("TestTag").v("Message");
                +}
                +;
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in kotlin class for verbose and tag`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                kotlin(
                    """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag").v("Message")
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag").v("Message")
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig
                @@ -4 +5,4 @@
                -       Timber.tag("TestTag").v("Message")
                +       if (BuildConfig.DEBUG) {
                +    Timber.tag("TestTag").v("Message")
                +}
                +
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in java class for verbose and tag with new line`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                java(
                    """
                import timber.log.Timber;
                public class Test {
                   public void test() {
                       Timber.tag("TestTag")
                           .v("Message");
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.java:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag")
                       ^
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.java line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig;
                @@ -4,2 +5,5 @@
                -       Timber.tag("TestTag")
                -           .v("Message");
                +        if (BuildConfig.DEBUG) {
                +     Timber.tag("TestTag")
                +.v("Message");
                + }
                +;
                """.trimIndent(),
            )
    }

    @Test
    fun `report errors if Timber in kotlin class for verbose and tag with new line`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                kotlin(
                    """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag")
                           .v("Message")
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Timber.v(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Timber.tag("TestTag")
                       ^
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -0,0 +1 @@
                +import com.cmgapps.BuildConfig
                @@ -4,2 +5,5 @@
                -       Timber.tag("TestTag")
                -           .v("Message")
                +        if (BuildConfig.DEBUG) {
                +     Timber.tag("TestTag")
                +.v("Message")
                + }
                +
                """.trimIndent(),
            )
    }

    @Test
    fun `not check 'd' method from unknown class`() {
        lint()
            .files(
                kotlin(
                    """
                class CustomClass {
                    fun d(text: String) {
                        println(text)
                    }
                }

                class OtherClass {
                    fun callD() {
                        val test = CustomClass()
                        test.d("Test")
                    }
                }
                """,
                ).indented(),
            ).run()
            .expect("No warnings.")
            .expectFixDiffs("")
    }

    @Test
    fun `not check if error log with 'e'`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                kotlin(
                    """
                import timber.log.Timber
                class Test {
                   fun test() {
                       Timber.tag("TestTag")
                           .e("Error")
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect("No warnings.")
            .expectFixDiffs("")
    }

    @Test
    fun `render quickfix with elvis operator correctly`() {
        lint()
            .files(
                timberStub,
                manifestStub,
                kotlin(
                    """
                import android.util.Log
                class Test {
                   fun test() {
                       Log.d("TestTag", null ?: "Message")
                   }
                }
                """,
                ).indented(),
            ).run()
            .expect(
                """
                src/Test.kt:4: Warning: The log call Log.d(...) should be conditional: surround with if (Log.isLoggable(...)) or if (BuildConfig.DEBUG) { ... } [LogDebugConditional]
                       Log.d("TestTag", null ?: "Message")
                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent(),
            ).expectFixDiffs(
                """
                Autofix for src/Test.kt line 4: Surround with `if (BuildConfig.DEBUG)`:
                @@ -1,0 +2 @@
                +import com.cmgapps.BuildConfig
                @@ -4 +5,4 @@
                -       Log.d("TestTag", null ?: "Message")
                +       if (BuildConfig.DEBUG) {
                +    Log.d("TestTag", null ?: "Message")
                +}
                +
                Autofix for src/Test.kt line 4: Surround with `if (Log.isLoggable(...))`:
                @@ -4 +4,4 @@
                -       Log.d("TestTag", null ?: "Message")
                +       if (Log.isLoggable("TestTag", Log.DEBUG)) {
                +    Log.d("TestTag", null ?: "Message")
                +}
                +
                """.trimIndent(),
            )
    }

    override fun getDetector(): Detector = LogDebugDetector()

    override fun getIssues(): List<Issue?> = listOf(LogDebugDetector.ISSUE)
}
