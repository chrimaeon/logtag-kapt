/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler.runner

import com.cmgapps.logtag.compiler.service.configurePlugin
import org.jetbrains.kotlin.js.test.runners.AbstractJsTest
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class AbstractJsBoxTest :
    AbstractJsTest(
        pathToTestDir = "compiler/compiler-plugin/testData/box",
        testGroupOutputDirPrefix = "box/",
        parser = FirParser.LightTree,
    ) {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
        EnvironmentBasedStandardLibrariesPathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        builder.configureJsBox()
    }
}

open class AbstractJsBoxOnlyTest :
    AbstractJsTest(
        pathToTestDir = "compiler/compiler-plugin/testData/boxJs",
        testGroupOutputDirPrefix = "box/",
        parser = FirParser.LightTree,
    ) {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
        EnvironmentBasedStandardLibrariesPathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        builder.configureJsBox()
    }
}

fun TestConfigurationBuilder.configureJsBox() {
    /*
     * Containers of different directives, which can be used in tests:
     * - ModuleStructureDirectives
     * - LanguageSettingsDirectives
     * - DiagnosticsDirectives
     * - FirDiagnosticsDirectives
     * - CodegenTestDirectives
     * - JvmEnvironmentConfigurationDirectives
     *
     * All of them are located in `org.jetbrains.kotlin.test.directives` package
     */
    defaultDirectives {
        +CodegenTestDirectives.DUMP_IR
        +FirDiagnosticsDirectives.FIR_DUMP
    }

    configurePlugin()
}
