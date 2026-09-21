/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.ir

import com.cmgapps.logtag.LOG_TAG_ANNOTATION_FQ_NAME
import com.cmgapps.logtag.LOG_TAG_PROPERTY_NAME
import com.cmgapps.logtag.LogTagPluginKey
import dev.zacsweers.metro.compiler.compat.CompatContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasEqualFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name

private const val DEFAULT_ANDROID_TAG_LENGTH_BEFORE_API_26 = 23

class LogTagIrGenerator(
    private val context: IrPluginContext,
    private val compatContext: CompatContext,
    private val androidMinSdkVersion: Int,
) : IrElementTransformerVoid() {
    private val activeTags = mutableListOf<String?>()

    override fun visitFile(declaration: IrFile): IrFile {
        val result = super.visitFile(declaration)
        @OptIn(UnsafeDuringIrConstructionAPI::class)
        declaration.declarations.removeAll { it is IrProperty && it.isGeneratedLogTag() }
        return result
    }

    override fun visitClass(declaration: IrClass): IrStatement =
        withTag(declaration.logTagOrNull()) {
            super.visitClass(declaration).also {
                @OptIn(UnsafeDuringIrConstructionAPI::class)
                declaration.declarations.removeAll { it is IrProperty && it.isGeneratedLogTag() }
            }
        }

    override fun visitFunction(declaration: IrFunction): IrStatement =
        with(compatContext) {
            val logTagAnnotation =
                declaration.annotationsCompat().firstOrNull { it.isLogTagAnnotation() }
            withTag(logTagAnnotation?.toLogTag(declaration.name.asString())) {
                super.visitFunction(declaration).also {
                    if (logTagAnnotation != null) {
                        declaration.replaceAnnotationsCompat(
                            declaration.annotationsCompat().filterNot { it === logTagAnnotation },
                        )
                    }
                }
            }
        }

    override fun visitCall(expression: IrCall): IrExpression {
        @OptIn(UnsafeDuringIrConstructionAPI::class)
        val property =
            expression.symbol.owner.correspondingPropertySymbol
                ?.owner
        val tag = activeTags.lastOrNull()
        if (tag != null && property?.isGeneratedLogTag() == true) {
            return tag.toIrConst(
                context.irBuiltIns.stringType,
                expression.startOffset,
                expression.endOffset,
            )
        }
        return super.visitCall(expression)
    }

    private inline fun <T> withTag(
        tag: String?,
        transform: () -> T,
    ): T {
        activeTags += tag ?: activeTags.lastOrNull()
        return try {
            transform()
        } finally {
            activeTags.removeLast()
        }
    }

    private fun IrClass.logTagOrNull(): String? =
        with(compatContext) {
            annotationsCompat()
                .firstOrNull { it.isLogTagAnnotation() }
                ?.toLogTag(name.asString())
        }

    private fun IrConstructorCall.isLogTagAnnotation(): Boolean =
        @OptIn(UnsafeDuringIrConstructionAPI::class)
        symbol.owner.parentAsClass.hasEqualFqName(LOG_TAG_ANNOTATION_FQ_NAME)

    private fun IrConstructorCall.toLogTag(default: String): String =
        with(compatContext) {
            val expression = getAnnotationArgumentCompat(Name.identifier("value")) as? IrConst
            expression?.value as? String
        }.orEmpty()
            .ifBlank {
                if (androidMinSdkVersion >= 26) {
                    default
                } else {
                    default.take(DEFAULT_ANDROID_TAG_LENGTH_BEFORE_API_26)
                }
            }

    private fun IrProperty.isGeneratedLogTag(): Boolean =
        name == LOG_TAG_PROPERTY_NAME &&
            (origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == LogTagPluginKey
}
