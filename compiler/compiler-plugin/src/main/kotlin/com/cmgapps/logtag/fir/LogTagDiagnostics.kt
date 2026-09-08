/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import com.cmgapps.logtag.fir.LogTagDiagnostics.LOG_TAG_OUTSIDE_ANNOTATED_SCOPE
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.psi.KtElement

internal object LogTagDiagnostics : KtDiagnosticsContainer() {
    override fun getRendererFactory(): BaseDiagnosticRendererFactory = LogTagRenderFactory

    val LOG_TAG_OUTSIDE_ANNOTATED_SCOPE by error0<KtElement>(
        positioningStrategy = SourceElementPositioningStrategies.NAME_IDENTIFIER,
    )
}

private object LogTagRenderFactory : BaseDiagnosticRendererFactory() {
    @Suppress("ktlint:standard:property-naming")
    override val MAP by KtDiagnosticFactoryToRendererMap("LogTag") {
        it.put(
            LOG_TAG_OUTSIDE_ANNOTATED_SCOPE,
            "LOG_TAG may only be used within a declaration annotated with @LogTag",
        )
    }
}
