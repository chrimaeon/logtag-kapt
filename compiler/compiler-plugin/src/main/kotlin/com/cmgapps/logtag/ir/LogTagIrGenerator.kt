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
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.getAnnotationValueOrNull
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class LogTagIrGenerator(
    private val context: IrPluginContext,
) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    override fun visitProperty(declaration: IrProperty) {
        val origin = declaration.origin

        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || origin.pluginKey != LogTagPluginKey) {
            return
        }

        if (declaration.name != LOG_TAG_PROPERTY_NAME) {
            return
        }

        val parentClass = declaration.parent as? IrClass ?: return
        val ownerClass = parentClass.let { if (it.isCompanion) it.parentAsClass else it }

        val annotation =
            ownerClass.annotations.findAnnotation(LOG_TAG_ANNOTATION_FQ_NAME)
                ?: return super.visitProperty(declaration)

        val logTag: String =
            annotation
                .getAnnotationValueOrNull<String>("value")
                .orEmpty()
                .ifBlank { ownerClass.name.asString().take(23) }

        declaration.backingField =
            context.irFactory
                .buildField {
                    name = declaration.name
                    type = context.irBuiltIns.stringType
                    isFinal = true
                    visibility = DescriptorVisibilities.PRIVATE
                }.apply {
                    parent = declaration.parent
                    initializer =
                        context.irFactory.createExpressionBody(
                            startOffset = startOffset,
                            endOffset = endOffset,
                            logTag.toIrConst(context.irBuiltIns.stringType, startOffset, endOffset),
                        )
                }
        declaration.getter?.apply {
            body =
                context.irFactory.createBlockBody(
                    startOffset,
                    endOffset,
                ) {
                    statements +=
                        IrReturnImpl(
                            startOffset,
                            endOffset,
                            context.irBuiltIns.nothingType,
                            symbol,
                            logTag.toIrConst(context.irBuiltIns.stringType, startOffset, endOffset),
                        )
                }
        }
        declaration.visibility = DescriptorVisibilities.PRIVATE
        declaration.annotations = emptyList()
    }

    override fun visitFunction(declaration: IrFunction) {
        val annotation =
            declaration.annotations.findAnnotation(LOG_TAG_ANNOTATION_FQ_NAME)
                ?: return super.visitFunction(declaration)

        val logTag: String =
            annotation
                .getAnnotationValueOrNull<String>("value")
                .orEmpty()
                .ifBlank { declaration.name.asString().take(23) }

        val logTagVariable =
            buildVariable(
                parent = declaration,
                startOffset = declaration.startOffset,
                endOffset = declaration.endOffset,
                origin = IrDeclarationOrigin.GeneratedByPlugin(LogTagPluginKey),
                name = LOG_TAG_PROPERTY_NAME,
                type = context.irBuiltIns.stringType,
            ).apply {
                initializer =
                    logTag.toIrConst(context.irBuiltIns.stringType, startOffset, endOffset)
            }

        when (val body = declaration.body) {
            is org.jetbrains.kotlin.ir.expressions.IrBlockBody -> {
                body.statements.add(0, logTagVariable)
            }

            is IrExpressionBody -> {
                declaration.body =
                    context.irFactory.createBlockBody(declaration.startOffset, declaration.endOffset) {
                        statements += logTagVariable
                        statements +=
                            IrReturnImpl(
                                declaration.startOffset,
                                declaration.endOffset,
                                context.irBuiltIns.nothingType,
                                declaration.symbol,
                                body.expression,
                            )
                    }
            }

            else -> {
                return
            }
        }

        declaration.body?.transformChildrenVoid(ReplaceResolverShimWithLocal(logTagVariable))
        declaration.annotations = emptyList()
        declaration.body?.acceptChildrenVoid(this)
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private class ReplaceResolverShimWithLocal(
        private val logTagVariable: IrVariable,
    ) : IrElementTransformerVoid() {
        override fun visitCall(expression: IrCall): IrExpression {
            val property =
                expression.symbol.owner.correspondingPropertySymbol
                    ?.owner
            if (
                property?.name == LOG_TAG_PROPERTY_NAME &&
                property.origin is IrDeclarationOrigin.GeneratedByPlugin &&
                (property.origin as IrDeclarationOrigin.GeneratedByPlugin).pluginKey == LogTagPluginKey &&
                property.parent is IrFile
            ) {
                return IrGetValueImpl(
                    expression.startOffset,
                    expression.endOffset,
                    logTagVariable.type,
                    logTagVariable.symbol,
                )
            }
            return super.visitCall(expression)
        }

        override fun visitFunction(declaration: IrFunction): IrStatement = declaration
    }
}
