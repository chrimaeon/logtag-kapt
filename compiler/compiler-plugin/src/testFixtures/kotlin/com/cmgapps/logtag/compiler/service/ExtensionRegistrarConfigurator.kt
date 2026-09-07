/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler.service

import com.cmgapps.logtag.LogTagCompilerRegistrar
import com.cmgapps.logtag.LogTagConfigurationKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.utils.bind

fun TestConfigurationBuilder.configurePlugin(
    configurationMap: Map<CompilerConfigurationKey<*>, Any> =
        mapOf(
            LogTagConfigurationKeys.ENABLED to true,
        ),
) {
    useConfigurators(::ExtensionRegistrarConfigurator.bind(configurationMap))
    configureAnnotations()
}

private class ExtensionRegistrarConfigurator(
    testServices: TestServices,
    private val configurationMap: Map<CompilerConfigurationKey<*>, Any>,
) : EnvironmentConfigurator(testServices) {
    private val registrar = LogTagCompilerRegistrar()

    @OptIn(ExperimentalCompilerApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration,
    ) {
        with(registrar) {
            configurationMap.forEach { (key, value) ->
                configuration.put(key, value)
            }
            registerExtensions(configuration)
        }
    }
}
