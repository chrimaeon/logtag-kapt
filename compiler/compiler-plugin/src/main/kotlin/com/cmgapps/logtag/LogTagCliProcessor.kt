/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import com.cmgapps.logtag.fir.LogTagFirExtensionRegistrar
import com.cmgapps.logtag.ir.LogTagIrGenerationExtension
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.cli.reportException
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CliOptionProcessingException
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.Name

object LogTagConfigurationKeys {
    val ENABLED: CompilerConfigurationKey<Boolean> = CompilerConfigurationKey("enabled")
    val ANDROID_MIN_SDK: CompilerConfigurationKey<Int> = CompilerConfigurationKey("android.minSdk")
    val TAG_NAME: CompilerConfigurationKey<String> = CompilerConfigurationKey("tagName")
}

@OptIn(ExperimentalCompilerApi::class)
class LogTagCliProcessor : CommandLineProcessor {
    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> =
        listOf(ENABLED_OPTION, ANDROID_MIN_SDK_OPTION, TAG_NAME_OPTION)

    companion object {
        val ENABLED_OPTION =
            CliOption(
                LogTagConfigurationKeys.ENABLED.toString(),
                "<true|false>",
                "sets the enabled state of the plugin",
                required = false,
                allowMultipleOccurrences = false,
            )
        val ANDROID_MIN_SDK_OPTION =
            CliOption(
                LogTagConfigurationKeys.ANDROID_MIN_SDK.toString(),
                "<Android min SDK version>",
                "sets the android.defaultConfig.minSdk the compiler should consider",
                required = false,
                allowMultipleOccurrences = false,
            )
        val TAG_NAME_OPTION =
            CliOption(
                LogTagConfigurationKeys.TAG_NAME.toString(),
                "<tag name>",
                "sets the tag name used to identify the log tag",
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
        ANDROID_MIN_SDK_OPTION -> configuration.put(LogTagConfigurationKeys.ANDROID_MIN_SDK, value.toInt())
        TAG_NAME_OPTION -> configuration.put(LogTagConfigurationKeys.TAG_NAME, value)
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}

@OptIn(ExperimentalCompilerApi::class)
class LogTagCompilerRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        if (!configuration.getBoolean(LogTagConfigurationKeys.ENABLED)) return

        val androidMinSdkVersion = configuration[LogTagConfigurationKeys.ANDROID_MIN_SDK, Int.MAX_VALUE]
        val tagName = configuration[LogTagConfigurationKeys.TAG_NAME, "LOG_TAG"]
        val tagNameIdentifier = Name.identifier(tagName)

        val compatContext =
            try {
                CompatContext.create()
            } catch (t: Throwable) {
                configuration.reportException(RuntimeException("Unable to create CompatContext", t))
                return
            }

        with(compatContext) {
            registerFirExtensionCompat(LogTagFirExtensionRegistrar(tagNameIdentifier))
            registerIrExtensionCompat(
                LogTagIrGenerationExtension(
                    compatContext,
                    androidMinSdkVersion,
                    tagNameIdentifier,
                ),
            )
        }
    }
}
