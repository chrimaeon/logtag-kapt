/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler.runner

import com.cmgapps.logtag.compiler.service.configurePlugin
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.directives.CodegenTestDirectives
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.runners.AbstractFirPhasedDiagnosticTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider

open class AbstractJvmDiagnosticTest : AbstractFirPhasedDiagnosticTest(FirParser.LightTree) {
    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider =
        EnvironmentBasedStandardLibrariesPathProvider

    override fun configure(builder: TestConfigurationBuilder) =
        with(builder) {
            super.configure(builder)
        /*
         * Containers of different directives, which can be used in tests:
         * - ModuleStructureDirectives
         * - LanguageSettingsDirectives
         * - DiagnosticsDirectives
         * - FirDiagnosticsDirectives
         *
         * All of them are located in `org.jetbrains.kotlin.test.directives` package
         */
            defaultDirectives {
                +FirDiagnosticsDirectives.FIR_DUMP
                +FirDiagnosticsDirectives.DISABLE_GENERATED_FIR_TAGS
                +JvmEnvironmentConfigurationDirectives.FULL_JDK

                +CodegenTestDirectives.IGNORE_DEXING // Avoids loading R8 from the classpath.
            }

            configurePlugin()
        }
}
