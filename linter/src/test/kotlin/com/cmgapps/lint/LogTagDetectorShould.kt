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

package com.cmgapps.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue
import org.junit.Ignore
import org.junit.jupiter.api.Test

@Suppress("UnstableApiUsage")
class LogTagDetectorShould : LintDetectorTest() {

    @Test
    fun `detect no warnings in java file`() {
        lint().files(
            java(
                """
                    package com.test;

                    @com.cmgapps.LogTag
                    public class JavaClassWithAnnotation {}
                """
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun `detect no warnings in java file with custom tag`() {
        lint().files(
            java(
                """
                    package com.test;

                    @com.cmgapps.LogTag("MyCustomTag")
                    public class JavaClassWithAnnotationTooLongName {}
                """
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Ignore("Kotlin files are not processed properly")
    @Test
    fun `detect no warnings in kotlin file`() {
        lint().files(
            kotlin(
                """
                    package com.test

                    @com.cmgapps.LogTag
                    class KotlinClassWithAnn
                """
            ).indented()
        )
            .run()
            .expectClean()
    }

    @Test
    fun `detect too long name`() {
        lint().files(
            java(
                """
                    package com.test;

                    @com.cmgapps.LogTag
                    public class JavaClassWithAnnotationTooLong {}
                """
            ).indented()
        )
            .run()
            .expect(
                """
                src/com/test/JavaClassWithAnnotationTooLong.java:4: Warning: Log tags are only allowed to be at most 23 characters long. You should set a custom log tag in the annotation or it will be truncated. [LogTagClassNameTooLong]
                public class JavaClassWithAnnotationTooLong {}
                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                0 errors, 1 warnings
                """.trimIndent()
            ).expectFixDiffs(
                """
                Fix for src/com/test/JavaClassWithAnnotationTooLong.java line 4: Add custom log tag:
                @@ -3 +3
                - @com.cmgapps.LogTag
                + @com.cmgapps.LogTag("|")
                """.trimIndent()
            )
    }

    override fun getDetector(): Detector = LogTagDetector()

    override fun getIssues(): List<Issue> {
        return listOf(LogTagDetector.ISSUE)
    }
}
