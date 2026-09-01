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
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.createBlockBody
import org.jetbrains.kotlin.ir.expressions.impl.IrReturnImpl
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.getAnnotationValueOrNull
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.toIrConst
import org.jetbrains.kotlin.ir.visitors.IrVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

class LogTagIrGenerator(
    private val context: IrPluginContext,
) : IrVisitorVoid() {
    override fun visitElement(element: IrElement) {
        when (element) {
            is IrDeclaration,
            is IrFile,
            is IrModuleFragment,
            -> {
                element.acceptChildrenVoid(this)
            }

            else -> {}
        }
    }

    override fun visitProperty(declaration: IrProperty) {
        val origin = declaration.origin

        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || origin.pluginKey != LogTagPluginKey) {
            return
        }

        if (declaration.name != LOG_TAG_PROPERTY_NAME) {
            return
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
}
