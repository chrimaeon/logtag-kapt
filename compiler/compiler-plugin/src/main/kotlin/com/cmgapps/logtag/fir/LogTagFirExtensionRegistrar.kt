/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.name.Name

internal class LogTagFirExtensionRegistrar(
    private val tagName: Name,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        registerDiagnosticContainers(LogTagDiagnostics)
        +::LogTagFirDeclarationGenerator.bind(tagName)
        +::LogTagFirAdditionalCheckersExtension.bind(tagName)
    }
}
