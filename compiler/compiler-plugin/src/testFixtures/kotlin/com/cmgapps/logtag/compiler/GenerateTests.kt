/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler

import com.cmgapps.logtag.compiler.runner.AbstractJsBoxOnlyTest
import com.cmgapps.logtag.compiler.runner.AbstractJsBoxTest
import com.cmgapps.logtag.compiler.runner.AbstractJsDiagnosticTest
import com.cmgapps.logtag.compiler.runner.AbstractJvmBoxTest
import com.cmgapps.logtag.compiler.runner.AbstractJvmDiagnosticTest
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main(args: Array<String>) {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(testsRoot = args[0], testDataRoot = args[1]) {
            testClass<AbstractJvmDiagnosticTest> {
                model("diagnostics")
            }

            testClass<AbstractJsDiagnosticTest> {
                model("diagnostics")
            }

            testClass<AbstractJvmBoxTest> {
                model("box")
                model("boxJvm")
            }

            testClass<AbstractJsBoxTest> {
                model("box")
            }

            testClass<AbstractJsBoxOnlyTest> {
                model("boxJs")
            }
        }
    }
}
