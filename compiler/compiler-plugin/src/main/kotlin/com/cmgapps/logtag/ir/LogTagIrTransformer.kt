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
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.getAnnotationValueOrNull
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class LogTagIrTransformer(
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
) : IrElementTransformerVoid() {
    override fun visitProperty(declaration: IrProperty): IrStatement {
        val origin = declaration.origin

        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || origin.pluginKey != LogTagPluginKey) {
            return super.visitProperty(declaration)
        }

        if (declaration.name != LOG_TAG_PROPERTY_NAME) {
            return super.visitProperty(declaration)
        }

        val ownerClass = declaration.parentAsClass.let { if (it.isCompanion) it.parentAsClass else it }

        val annotation =
            ownerClass.annotations.findAnnotation(LOG_TAG_ANNOTATION_FQ_NAME)
                ?: return super.visitProperty(declaration)

        val logTag: String =
            annotation
                .getAnnotationValueOrNull<String>("value")
                .orEmpty()
                .ifBlank { ownerClass.name.asString().take(23) }

        messageCollector.report(
            CompilerMessageSeverity.INFO,
            """
            |Add property implementation
            |   private val ${declaration.name} =  "$logTag"
            |on
            |   ${declaration.parentAsClass.classId?.asString()}
            """.trimMargin(),
        )

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
