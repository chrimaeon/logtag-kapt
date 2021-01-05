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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class LogTagProcessor : AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(LogTag::class.java.canonicalName)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty() || annotations.find { it.qualifiedName.contentEquals(LogTag::class.java.canonicalName) } == null) {
            return false
        }

        roundEnv.getElementsAnnotatedWith(LogTag::class.java).map {
            if (!(it.kind.isClass || it.kind.isInterface)) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "LogTag annotation can only be applied to a class/interface"
                )
            }
            if (!it.modifiers.contains(Modifier.PUBLIC)) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "LogTag annotation can only be applied to public classes"
                )
            }
            AnnotatedElement(it as TypeElement)
        }.forEach { element ->
            val propertySpec = PropertySpec.builder("LOG_TAG", String::class)
                .receiver(element.className)
                .addOriginatingElement(element.element)
                .getter(
                    FunSpec.getterBuilder()
                        .addModifiers(KModifier.INLINE)
                        .addStatement("return %S", element.className.simpleName)
                        .build()
                ).build()

            FileSpec.builder(element.className.packageName, "__${element.className.simpleName}LogTag")
                .addProperty(propertySpec)
                .addAnnotation(
                    AnnotationSpec.builder(Suppress::class).addMember("%S", "SpellCheckingInspection")
                        .addMember("%S", "RedundantVisibilityModifier")
                        .addMember("%S", "unused")
                        .build()
                )
                .build().writeTo(filer)
        }

        return true
    }
}

data class AnnotatedElement(val element: TypeElement) {
    val className: ClassName = element.asClassName()
}
