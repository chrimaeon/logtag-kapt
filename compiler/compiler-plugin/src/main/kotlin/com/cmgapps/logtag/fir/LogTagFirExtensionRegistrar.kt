/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class LogTagFirExtensionRegistrar(
    private val messageCollector: MessageCollector,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::LogTagFirCheckersExtension
        +::LogTagFirDeclarationGenerator.bind(messageCollector)
    }
}
