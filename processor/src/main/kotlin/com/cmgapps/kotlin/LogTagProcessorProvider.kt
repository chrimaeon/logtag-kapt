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

import com.cmgapps.LogTag
import com.google.auto.service.AutoService
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Origin
import com.google.devtools.ksp.symbol.Visibility
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import javax.lang.model.element.Modifier

@AutoService(SymbolProcessorProvider::class)
public class LogTagProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = LogTagSymbolProcessor(environment)
}

private class LogTagSymbolProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(LOG_TAG_ANNOTATION_NAME).filterIsInstance(KSClassDeclaration::class.java)
            .forEach { type ->
                logger.check(type.getVisibility() !in INVALID_VISIBILITIES, type) {
                    "@LogTag cannot be applied to private classes"
                }

                when(type.origin) {
                    Origin.KOTLIN -> resolver.generateKotlinProperty(type)
                    Origin.JAVA -> resolver.generateJavaClass(type)
                    else -> logger.error("@LogTag applied to a unknown origin ${type.origin.name}")
                }
            }
        resolver.getSymbolsWithAnnotation(LOG_TAG_ANNOTATION_NAME).filter { !KSClassDeclaration::class.java.isInstance(it) }.forEach {
            logger.warn("@LogTag can only be applied to class-like declarations", it)
        }
        return emptyList()
    }

    private fun Resolver.generateKotlinProperty(element: KSClassDeclaration) {
        val visibility = when (element.getVisibility()) {
            Visibility.PUBLIC -> KModifier.PUBLIC
            Visibility.INTERNAL -> KModifier.INTERNAL
            Visibility.PROTECTED -> KModifier.PROTECTED
            else -> KModifier.PRIVATE
        }

        val propertySpec = PropertySpec.builder("LOG_TAG", String::class, visibility)
            .receiver(ClassName(element.packageName.asString(), element.simpleName.asString()))
            .getter(
                FunSpec.getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %S", getTag(element))
                    .build()
            ).build()

        FileSpec.builder(element.packageName.asString(), "${element.simpleName.asString()}LogTag")
            .addProperty(propertySpec)
            .addAnnotation(
                AnnotationSpec.builder(Suppress::class).addMember("%S", "SpellCheckingInspection")
                    .addMember("%S", "RedundantVisibilityModifier")
                    .addMember("%S", "unused")
                    .build()
            )
            .build().writeTo(codeGenerator, element.containingFile!!)
    }

    private fun Resolver.generateJavaClass(element: KSClassDeclaration) {
        val field =
            FieldSpec.builder(String::class.java, "LOG_TAG", Modifier.STATIC, Modifier.FINAL)
                .initializer("\$S", getTag(element)).build()
        val clazz =
            TypeSpec.classBuilder("${element.simpleName.asString()}LogTag")
                .addField(field).build()

        JavaFile.builder(element.packageName.asString(), clazz).build().writeTo(codeGenerator, element.containingFile!!)
    }

    private fun Resolver.getTag(element: KSClassDeclaration): String {
        val logTagType = this.getClassDeclarationByName<LogTag>()!!.asType(emptyList())

        val logTagAnnotation = element.annotations.find { it.annotationType.resolve() == logTagType }
        val logTag = logTagAnnotation?.arguments?.find { it.name?.asString() == "value" }?.value as? String

        if (!logTag.isNullOrBlank()) {
            return logTag
        }

        return element.simpleName.asString().let {
            if (it.length > 23) {
                logger.warn(
                    "Class name \"$it\" is to long for a log tag. Max. length is 23. Class name will be truncated.",
                    element
                )
                it.substring(0..22)
            } else {
                it
            }
        }
    }

    companion object {
        val LOG_TAG_ANNOTATION_NAME: String = LogTag::class.qualifiedName!!
        val INVALID_VISIBILITIES = listOf(Visibility.PRIVATE, Visibility.LOCAL)
    }
}

private inline fun KSPLogger.check(condition: Boolean, element: KSNode?, onFalseCondition: () -> String) {
    if (!condition) {
        error(onFalseCondition(), element)
    }
}

private fun FileSpec.writeTo(codeGenerator: CodeGenerator, originatingFile: KSFile) {
    val dependencies = Dependencies(false, originatingFile)
    val file = codeGenerator.createNewFile(dependencies, packageName, name)
    OutputStreamWriter(file, UTF_8).use(::writeTo)
}

private fun JavaFile.writeTo(codeGenerator: CodeGenerator, originatingFile: KSFile) {
    val dependencies = Dependencies(false, originatingFile)
    val file = codeGenerator.createNewFile(dependencies, packageName, typeSpec.name, "java")
    OutputStreamWriter(file, UTF_8).use(::writeTo)
}
