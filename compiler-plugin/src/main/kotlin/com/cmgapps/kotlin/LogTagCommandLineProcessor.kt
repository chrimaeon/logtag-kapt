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

package com.cmgapps.kotlin

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal const val srcGenDirName = "src-gen-dir"
internal val srcGenDirKey = CompilerConfigurationKey.create<String>(srcGenDirName)

@AutoService(CommandLineProcessor::class)
class LogTagCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "com.cmgapps.logtag.compiler"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = srcGenDirName,
            valueDescription = "<file-path>",
            description = "Path to directory in which Log Tag specific code should be generated",
            required = true,
            allowMultipleOccurrences = false
        )
    )

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option.optionName) {
            srcGenDirName -> configuration.put(srcGenDirKey, value)
        }
    }
}
