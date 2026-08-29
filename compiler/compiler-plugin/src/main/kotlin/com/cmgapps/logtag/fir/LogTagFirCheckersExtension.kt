/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import com.cmgapps.logtag.LOG_TAG_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.KtDiagnosticsContainer
import org.jetbrains.kotlin.diagnostics.SourceElementPositioningStrategies
import org.jetbrains.kotlin.diagnostics.error0
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirRegularClassChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneTypeOrNull
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtClass

internal class LogTagFirCheckersExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers: DeclarationCheckers =
        object : DeclarationCheckers() {
            override val regularClassCheckers: Set<FirRegularClassChecker> =
                setOf(LogTagFirRegularClassChecker)
        }

    private object LogTagFirRegularClassChecker : FirRegularClassChecker(
        mppKind = MppCheckerKind.Common,
    ) {
        context(
            context: CheckerContext,
            reporter: DiagnosticReporter
        )
        override fun check(declaration: FirRegularClass) {
            if (!declaration.hasAnnotation(ClassId.topLevel(LOG_TAG_ANNOTATION_FQ_NAME))) return

            val errorFactory =
                when {
                    declaration.classKind != ClassKind.CLASS -> {
                        Diagnostics.LogTagOnNonClass
                    }

                    else -> {
                        null
                    }
                }

            if (errorFactory != null) {
                reporter.reportOn(
                    source = declaration.source,
                    factory = errorFactory,
                )
            }
        }

        private fun FirDeclaration.hasAnnotation(annotation: ClassId): Boolean =
            annotations.any { firAnnotation ->
                firAnnotation.classId() == annotation
            }

        private fun FirAnnotation.classId(): ClassId? = annotationTypeRef.coneTypeOrNull?.classId
    }

    private object Diagnostics : KtDiagnosticsContainer() {
        val LogTagOnNonClass by error0<KtClass>(
            positioningStrategy = SourceElementPositioningStrategies.NAME_IDENTIFIER,
        )

        override fun getRendererFactory(): BaseDiagnosticRendererFactory = DiagnosticRendererFactory
    }

    private object DiagnosticRendererFactory : BaseDiagnosticRendererFactory() {
        @Suppress("ktlint:standard:property-naming")
        override val MAP by KtDiagnosticFactoryToRendererMap("LogTag") {
            it.put(
                factory = Diagnostics.LogTagOnNonClass,
                message = "LogTag can only be applied to a class",
            )
        }
    }
}
