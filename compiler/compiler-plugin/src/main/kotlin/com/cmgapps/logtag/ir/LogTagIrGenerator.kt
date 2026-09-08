/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.ir

import com.cmgapps.logtag.LOG_TAG_ANNOTATION_FQ_NAME
import com.cmgapps.logtag.LOG_TAG_PROPERTY_NAME
import com.cmgapps.logtag.LogTagPluginKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrAnnotation
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.getConstArgument
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

@OptIn(UnsafeDuringIrConstructionAPI::class)
class LogTagIrGenerator(
    private val context: IrPluginContext,
) : IrElementTransformerVoid() {
    private val activeTags = mutableListOf<String?>()

    override fun visitFile(declaration: IrFile): IrFile {
        val result = super.visitFile(declaration)
        declaration.declarations.removeAll { it is IrProperty && it.isGeneratedLogTag() }
        return result
    }

    override fun visitClass(declaration: IrClass): IrStatement =
        withTag(declaration.logTagOrNull()) {
            super.visitClass(declaration).also {
                declaration.declarations.removeAll { it is IrProperty && it.isGeneratedLogTag() }
            }
        }

    override fun visitFunction(declaration: IrFunction): IrStatement {
        val logTagAnnotation = declaration.annotations.findAnnotation(LOG_TAG_ANNOTATION_FQ_NAME)
        return withTag(logTagAnnotation?.toLogTag(declaration.name.asString())) {
            super.visitFunction(declaration).also {
                if (logTagAnnotation != null) {
                    declaration.annotations = declaration.annotations.filterNot { it === logTagAnnotation }
                }
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
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

    private fun IrClass.logTagOrNull(): String? = annotations.findAnnotation(LOG_TAG_ANNOTATION_FQ_NAME)?.toLogTag(name.asString())

    private fun IrAnnotation.toLogTag(default: String): String =
        this
            .getConstArgument<String>("value")
            .orEmpty()
            .ifBlank { default.take(23) }

    private fun IrProperty.isGeneratedLogTag(): Boolean =
        name == LOG_TAG_PROPERTY_NAME &&
            (origin as? IrDeclarationOrigin.GeneratedByPlugin)?.pluginKey == LogTagPluginKey
}
