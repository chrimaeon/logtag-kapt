/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import com.cmgapps.logtag.LogTagPluginNames.FIELD_ORIGIN
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.getAnnotationValueOrNull
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class LogTagIrTransformer(
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
) : IrElementTransformerVoid() {
    override fun visitProperty(declaration: IrProperty): IrStatement {
        if (declaration.origin != FIELD_ORIGIN && declaration.name != LogTagPluginNames.LOG_TAG_PROPERTY_NAME) {
            return super.visitProperty(declaration)
        }

        val annotation =
            declaration.annotations.findAnnotation(LogTagPluginNames.LOG_TAG_ANNOTATION_ID.asSingleFqName())
                ?: return super.visitProperty(declaration)

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            "Setting property ${declaration.name} with annotation ${annotation.dumpKotlinLike()}",
        )

        val logTag: String =
            annotation
                .getAnnotationValueOrNull<String>("value")
                .orEmpty()
                .ifBlank {
                    declaration.parentAsClass.name
                        .asString()
                        .take(23)
                }

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
                            IrConstImpl.string(
                                startOffset,
                                endOffset,
                                context.irBuiltIns.stringType,
                                logTag,
                            ),
                        )
                }
        declaration.visibility = DescriptorVisibilities.PRIVATE
        declaration.annotations = emptyList()

        return super.visitProperty(declaration)
    }
}
