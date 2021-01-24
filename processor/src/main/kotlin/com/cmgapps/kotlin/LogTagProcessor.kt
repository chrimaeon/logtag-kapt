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
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import javax.annotation.Generated
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic
import com.squareup.javapoet.AnnotationSpec as JavaAnnotationSpec
import com.squareup.javapoet.ClassName as JavaClassName
import com.squareup.kotlinpoet.AnnotationSpec as KotlinAnnotationSpec
import com.squareup.kotlinpoet.ClassName as KotlinClassName

@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@AutoService(Processor::class)
class LogTagProcessor : AbstractProcessor() {

    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var kaptKotlinGeneratedDir: File

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
        kaptKotlinGeneratedDir = File(
            processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                ?: error("Can't find the target directory for generated Kotlin files.")
        ).apply {
            mkdirs()
        }
    }

    override fun getSupportedAnnotationTypes(): Set<String> = setOf(LogTag::class.java.canonicalName)
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
    override fun getSupportedOptions(): Set<String> = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)

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

                // consume the annotation anyway
                return true
            }

            if (!it.modifiers.contains(Modifier.PUBLIC)) {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "LogTag annotation can only be applied to public classes"
                )

                // consume the annotation anyway
                return true
            }
            AnnotatedElement(it as TypeElement)
        }.forEach { element ->
            if (element.isKotlin) generateKotlinExtensionFunction(element) else generateJavaClass(element)
        }

        return true
    }

    private fun generateJavaClass(element: AnnotatedElement) {
        val field =
            FieldSpec.builder(String::class.java, "LOG_TAG", Modifier.STATIC, Modifier.FINAL)
                .initializer("\$S", getTag(element))
                .build()
        val clazz =
            TypeSpec.classBuilder("${element.kotlinClassName.simpleName}LogTag")
                .addOriginatingElement(element.element)
                .addAnnotation(
                    JavaAnnotationSpec.builder(Generated::class.java)
                        .addMember("value", "\$S", LogTagProcessor::class.java.canonicalName)
                        .addMember("date", "\$S", DATE_FORMATTER.format(Date()))
                        .build()
                )
                .addField(field).build()

        JavaFile.builder(element.kotlinClassName.packageName, clazz)
            .addFileComment("Automatically generated file. DO NOT MODIFY")
            .build().writeTo(filer)
    }

    private fun generateKotlinExtensionFunction(element: AnnotatedElement) {

        val propertySpec = PropertySpec.builder("LOG_TAG", String::class)
            .receiver(element.kotlinClassName)
            .addOriginatingElement(element.element)
            .getter(
                FunSpec.getterBuilder()
                    .addModifiers(KModifier.INLINE)
                    .addStatement("return %S", getTag(element))
                    .build()
            ).build()

        FileSpec.builder(element.kotlinClassName.packageName, "${element.kotlinClassName.simpleName}LogTag")
            .addProperty(propertySpec)
            .addAnnotation(
                KotlinAnnotationSpec.builder(Suppress::class).addMember("%S", "SpellCheckingInspection")
                    .addMember("%S", "RedundantVisibilityModifier")
                    .addMember("%S", "unused")
                    .build()
            )
            .addAnnotation(
                KotlinAnnotationSpec.builder(Generated::class)
                    .addMember("value=[%S]", LogTagProcessor::class.java.canonicalName)
                    .addMember("date=%S", DATE_FORMATTER.format(Date()))
                    .build()
            )
            .addComment("Automatically generated file. DO NOT MODIFY")
            .build().writeTo(kaptKotlinGeneratedDir)
    }

    private fun getTag(element: AnnotatedElement): String {
        val logTag = element.getLogTagAnnotation().value
        if (logTag.isNotBlank()) {
            return logTag
        }

        return element.javaClassName.simpleName().let {
            if (it.length > 23) {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "Class name \"$it\" is to long for a log tag. Max. length is 23. Class name will be truncated."
                )
                it.substring(0..21) + Typography.ellipsis
            } else {
                it
            }
        }
    }

    inner class AnnotatedElement(val element: TypeElement) {
        private val elementUtils: Elements = processingEnv.elementUtils
        val kotlinClassName: KotlinClassName = KotlinClassName(
            elementUtils.getPackageOf(element).qualifiedName.toString(),
            element.simpleName.toString()
        )

        val javaClassName: JavaClassName = JavaClassName.get(
            elementUtils.getPackageOf(element).qualifiedName.toString(),
            element.simpleName.toString()
        )

        fun getLogTagAnnotation(): LogTag {
            return element.getAnnotation(LogTag::class.java)
        }

        val isKotlin: Boolean
            get() {
                val metaDataClass = Class.forName("kotlin.Metadata").asSubclass(Annotation::class.java)
                return element.getAnnotation(metaDataClass) != null
            }
    }

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }
}
