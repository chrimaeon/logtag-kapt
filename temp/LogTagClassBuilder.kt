/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.kotlin

import com.cmgapps.logtag.LogTag
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import com.cmgapps.logtag.annotation.LogTag as LogTagAnnotation

private val logTagAnnotationFqName = FqName(LogTagAnnotation::class.java.canonicalName)
private val logTagFqName = FqName(LogTag::class.java.canonicalName)

internal class LogTagClassBuilder(
    private val delegateBuilder: ClassBuilder,
    classOrigin: JvmDeclarationOrigin,
    private val messageCollector: MessageCollector
) : DelegatingClassBuilder() {
    override fun getDelegate(): ClassBuilder = delegateBuilder

    private val logTagAnnotation = classOrigin.descriptor?.original?.annotations?.findAnnotation(logTagAnnotationFqName)

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {

        val className = FqName(Type.getObjectType(delegateBuilder.thisName).className)
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)

        if (className != logTagFqName && name == "getLogTag" && desc == "()Ljava/lang/String;") {
            InstructionAdapter(original).apply {
                aconst(className.getLogTag(logTagAnnotation, messageCollector))
                areturn(Type.getType(String::class.java))
            }
        }

        return original

        // return object : MethodVisitor(Opcodes.ASM5, original) {
        //     override fun visitMethodInsn(
        //         opcode: Int,
        //         owner: String?,
        //         name: String?,
        //         descriptor: String?,
        //         isInterface: Boolean
        //     ) {
        //         if (owner != "timber/log/Timber") {
        //             return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        //         }
        //
        //         if (!LOG_METHOD_NAMES.contains(name)) {
        //             return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        //         }
        //
        //         // logging method not invoked static so assume `tag` is already called
        //         if (opcode != Opcodes.INVOKESTATIC) {
        //             return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        //         }
        //
        //         InstructionAdapter(this).apply {
        //             aconst(className.getLogTag(logTagAnnotation, messageCollector))
        //             invokestatic("timber/log/Timber", "tag", "(Ljava/lang/String;)Ltimber/log/Timber\$Tree;", false)
        //             // pop the Timber.Tree so original log call can be executed
        //             pop()
        //         }
        //
        //         return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        //     }
        // }
    }

    private fun FqName.getLogTag(logTagAnnotation: AnnotationDescriptor?, messageCollector: MessageCollector): String {
        val logTagValue = logTagAnnotation?.allValueArguments?.get(Name.identifier("value"))?.value as? String

        if (!logTagValue.isNullOrBlank()) {
            return logTagValue
        }

        return this.shortName().asString().let {
            if (it.length > 23) {
                messageCollector.report(
                    CompilerMessageSeverity.WARNING,
                    "Class name \"$it\" exceeds max. length of 23 for a log tag. Class name will be truncated." +
                        " Add the @${logTagAnnotationFqName.asString()} annotation with a custom tag to override"
                )
                it.substring(0..21) + Typography.ellipsis
            } else {
                it
            }
        }
    }
}
