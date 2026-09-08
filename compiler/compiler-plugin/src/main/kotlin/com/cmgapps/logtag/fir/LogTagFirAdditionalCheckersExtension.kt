/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.fir

import com.cmgapps.logtag.LOG_TAG_ANNOTATION_CLASS_ID
import com.cmgapps.logtag.LOG_TAG_PROPERTY_NAME
import com.cmgapps.logtag.LogTagPluginKey
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirExpressionChecker
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

internal class LogTagFirAdditionalCheckersExtension(
    session: FirSession,
) : FirAdditionalCheckersExtension(session) {
    override val expressionCheckers: ExpressionCheckers =
        object : ExpressionCheckers() {
            override val propertyAccessExpressionCheckers: Set<FirExpressionChecker<FirPropertyAccessExpression>> =
                setOf(LogTagPropertyAccessChecker)
        }

    private object LogTagPropertyAccessChecker :
        FirExpressionChecker<FirPropertyAccessExpression>(MppCheckerKind.Common) {
        context(context: CheckerContext, reporter: DiagnosticReporter)
        override fun check(expression: FirPropertyAccessExpression) {
            val property =
                (expression.calleeReference as? FirResolvedNamedReference)
                    ?.resolvedSymbol as? FirPropertySymbol
                    ?: return
            if (!property.isGeneratedLogTag()) return

            if (context.containingDeclarations.any {
                    it.hasAnnotation(
                        LOG_TAG_ANNOTATION_CLASS_ID,
                        context.session,
                    )
                }
            ) {
                return
            }

            expression.source?.let { source ->
                reporter.reportOn(source, LogTagDiagnostics.LOG_TAG_OUTSIDE_ANNOTATED_SCOPE, context)
            }
        }

        @OptIn(SymbolInternals::class)
        private fun FirPropertySymbol.isGeneratedLogTag(): Boolean =
            callableId?.callableName == LOG_TAG_PROPERTY_NAME &&
                (fir.origin as? FirDeclarationOrigin.Plugin)?.key == LogTagPluginKey
    }
}
