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

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.ConstantEvaluator
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

@Suppress("UnstableApiUsage", "unused")
class LogTagDetector : Detector(), Detector.UastScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler = UastHandler(context)

    private class UastHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitClass(node: UClass) {
            val allAnnotations = context.evaluator.getAllAnnotations(node as UAnnotated, false)
            val annotation = allAnnotations.firstOrNull { it.qualifiedName == "com.cmgapps.LogTag" } ?: return

            val className = node.name ?: return

            if (className.length <= 23) return

            val valueAttribute = annotation.findAttributeValue("value")
            val hasValue = valueAttribute != null && run {
                val value = ConstantEvaluator.evaluate(context, valueAttribute) as? String
                value != null && value.isNotBlank()
            }

            if (hasValue) return

            context.report(
                ISSUE,
                node,
                context.getNameLocation(node),
                "Log tags are only allowed to be at most 23 characters long. " +
                    "You should set a custom log tag in the annotation or it will be truncated.",
                LintFix.create()
                    .name("Add custom log tag")
                    .replace()
                    .text(annotation.asSourceString())
                    .range(context.getNameLocation(annotation))
                    .shortenNames()
                    .reformat(true)
                    .with("""@${annotation.qualifiedName}("")""")
                    .select("""@${annotation.qualifiedName}\("()"\)""")
                    .build()
            )
        }
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "LogTagClassNameTooLong",
            briefDescription = "Log tag too long",
            explanation = """
                Checks if the class' name annotated with @com.cmgapps.LogTag is at most 23 characters long
                 and does not have a custom log tag specified.
            """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(LogTagDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }
}
