/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler.runner

import com.cmgapps.logtag.LogTagConfigurationKeys
import com.cmgapps.logtag.compiler.service.configurePlugin
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.js.test.runners.AbstractJsTest
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

abstract class AbstractJsBoxTest(
    pathToTestDir: String = "compiler/compiler-plugin/testData/box",
) : AbstractJsTest(
        pathToTestDir = pathToTestDir,
        testGroupOutputDirPrefix = "box/",
        parser = FirParser.LightTree,
    ) {
    open val configurationMap: Map<CompilerConfigurationKey<*>, Any> =
        mapOf(
            LogTagConfigurationKeys.ENABLED to true,
        )

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
        EnvironmentBasedStandardLibrariesPathProvider

    override fun configure(builder: TestConfigurationBuilder) {
        with(builder) {
            super.configure(this)
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

            configurePlugin(configurationMap)
        }
    }
}

abstract class AbstractJsBoxOnlyTest :
    AbstractJsBoxTest(
        pathToTestDir = "compiler/compiler-plugin/testData/boxJs",
    )

abstract class AbstractJsCustomTagBoxTest :
    AbstractJsBoxTest(
        pathToTestDir = "compiler/compiler-plugin/testData/boxCustom",
    ) {
    override val configurationMap: Map<CompilerConfigurationKey<*>, Any>
        get() =
            super.configurationMap.toMutableMap().apply {
                put(LogTagConfigurationKeys.TAG_NAME, "MY_TAG")
            }
}
