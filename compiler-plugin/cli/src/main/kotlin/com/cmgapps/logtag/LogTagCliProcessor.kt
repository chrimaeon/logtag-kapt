/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.config.messageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

object LogTagConfigurationKeys {
    val ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey.create("plugin enabled or not")
}

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CommandLineProcessor::class)
class LogTagCliProcessor : CommandLineProcessor {
    override val pluginId: String = LogTagPluginNames.PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(ENABLED_OPTION)

    companion object {
        val ENABLED_OPTION =
            CliOption(
                LogTagPluginNames.ENABLED_OPTION_NAME,
                "<true|false>",
                "sets the enabled state of the plugin",
                required = false,
                allowMultipleOccurrences = false,
            )
    }

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) = when (option) {
        ENABLED_OPTION -> configuration.put(LogTagConfigurationKeys.ENABLED, value.toBoolean())
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}

@OptIn(ExperimentalCompilerApi::class)
@AutoService(CompilerPluginRegistrar::class)
class LogTagCompilerRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.messageCollector

        val enabled = configuration.getBoolean(LogTagConfigurationKeys.ENABLED)
        val useFir = configuration.getBoolean(CommonConfigurationKeys.USE_FIR)

        val message =
            buildString {
                append("XOR compiler plugin is ")
                if (enabled) {
                    append("enabled")
                    if (useFir) {
                        append(" and uses FIR")
                    }
                } else {
                    append("disabled")
                }
            }

        if (!enabled) {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                message,
            )
            return
        }

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            message,
        )

        FirExtensionRegistrarAdapter.registerExtension(FirLogTagExtensionRegistrar(messageCollector = messageCollector))

        IrGenerationExtension.registerExtension(LogTagIrGenerationExtension(messageCollector = messageCollector))
    }
}
