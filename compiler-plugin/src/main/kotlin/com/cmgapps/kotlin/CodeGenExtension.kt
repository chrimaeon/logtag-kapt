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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.codegen.CompilationException
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierTypeOrDefault
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisHandlerExtension
import java.io.File

class CodeGenExtension(private val codeGenDir: File) : AnalysisHandlerExtension {
    private var didRecompile = false

    override fun doAnalysis(
        project: Project,
        module: ModuleDescriptor,
        projectContext: ProjectContext,
        files: Collection<KtFile>,
        bindingTrace: BindingTrace,
        componentProvider: ComponentProvider
    ): AnalysisResult? {
        return if (!didRecompile) AnalysisResult.EMPTY else null
    }

    override fun analysisCompleted(
        project: Project,
        module: ModuleDescriptor,
        bindingTrace: BindingTrace,
        files: Collection<KtFile>
    ): AnalysisResult? {

        if (didRecompile) return null
        didRecompile = true

        codeGenDir.listFiles()?.forEach {
            check(it.deleteRecursively()) {
                "Could not clean file: $it"
            }
        }

        files.asSequence()
            .flatMap { it.classes() }
            .filter { it.hasAnnotation(logTagFqName) }
            .onEach { clazz ->
                if (clazz.visibilityModifierTypeOrDefault().value == KtTokens.PRIVATE_KEYWORD.value) {
                    throw CompilationException(
                        "${clazz.fqName} is annotated with @${logTagFqName.asString()}, but the " +
                            "class is not public. Only public types are supported",
                        null,
                        clazz.identifyingElement
                    )
                }
            }.forEach { clazz ->
                val packageName = clazz.containingKtFile.packageFqName.asString()
                val receiverClassName = ClassName(packageName, clazz.nameAsSafeName.asString())

                check(codeGenDir.exists() || codeGenDir.mkdirs()) {
                    "Could not generate package directory: $codeGenDir"
                }

                val modifier =
                    if (clazz.visibilityModifierTypeOrDefault().value == KtTokens.INTERNAL_KEYWORD.value) {
                        KModifier.INTERNAL
                    } else {
                        KModifier.PUBLIC
                    }

                val propertySpec = PropertySpec.builder("LOG_TAG", String::class, modifier)
                    .receiver(receiverClassName)
                    .getter(
                        FunSpec.getterBuilder()
                            .addModifiers(KModifier.INLINE)
                            .addStatement("return %S", clazz.getTag(module))
                            .build()
                    ).build()

                FileSpec.builder(packageName, "${receiverClassName.simpleName}LogTag")
                    .addProperty(propertySpec)
                    .addAnnotation(
                        AnnotationSpec.builder(Suppress::class).addMember("%S", "SpellCheckingInspection")
                            .addMember("%S", "RedundantVisibilityModifier")
                            .addMember("%S", "unused")
                            .build()
                    )
                    .build().writeTo(codeGenDir)
            }

        return AnalysisResult.RetryWithAdditionalRoots(
            bindingTrace.bindingContext,
            module,
            emptyList(),
            listOf(codeGenDir),
            true
        )
    }

    private fun KtClassOrObject.getTag(module: ModuleDescriptor): String {
        val logTagValue =
            module.findClassAcrossModuleDependencies(ClassId.topLevel(fqName!!))?.annotations?.findAnnotation(
                logTagFqName
            )?.allValueArguments?.get(
                Name.identifier("value")
            )?.value as? String

        if (!logTagValue.isNullOrBlank()) {
            return logTagValue
        }

        return name?.let {
            if (it.length > 23) {
                // messager.printMessage(
                //     Diagnostic.Kind.WARNING,
                //     "Class name \"$it\" is to long for a log tag. Max. length is 23. Class name will be truncated."
                // )
                it.substring(0..22)
            } else {
                it
            }
        } ?: "LogTag"
    }
}

private val logTagFqName = FqName(LogTag::class.java.canonicalName)

private fun KtFile.classes(): Sequence<KtClassOrObject> {
    val children = findChildrenByClass(KtClassOrObject::class.java)

    return generateSequence(children.toList()) { list ->
        list
            .flatMap {
                it.declarations.filterIsInstance<KtClassOrObject>()
            }
            .ifEmpty { null }
    }.flatMap { it.asSequence() }
}

internal fun KtAnnotated.hasAnnotation(fqName: FqName): Boolean {
    return findAnnotation(fqName) != null
}

internal val jvmSuppressWildcardsFqName = FqName(JvmSuppressWildcards::class.java.canonicalName)
private val kotlinAnnotations = listOf(jvmSuppressWildcardsFqName)

internal fun KtAnnotated.findAnnotation(fqName: FqName): KtAnnotationEntry? {
    val annotationEntries = annotationEntries
    if (annotationEntries.isEmpty()) return null

    // Look first if it's a Kotlin annotation. These annotations are usually not imported and the
    // remaining checks would fail.
    annotationEntries.firstOrNull { annotation ->
        kotlinAnnotations
            .any { kotlinAnnotationFqName ->
                val text = annotation.text
                text.startsWith("@${kotlinAnnotationFqName.shortName()}") ||
                    text.startsWith("@$kotlinAnnotationFqName")
            }
    }?.let { return it }

    // Check if the fully qualified name is used, e.g. `@com.cmgapps.LogTag`.
    val annotationEntry = annotationEntries.firstOrNull {
        it.text.startsWith("@${fqName.asString()}")
    }
    if (annotationEntry != null) return annotationEntry

    // Check if the simple name is used, e.g. `@Module`.
    val annotationEntryShort = annotationEntries
        .firstOrNull {
            it.shortName == fqName.shortName()
        }
        ?: return null

    val importPaths = containingKtFile.importDirectives.mapNotNull { it.importPath }

    // If the simple name is used, check that the annotation is imported.
    val hasImport = importPaths.any { it.fqName == fqName }
    if (hasImport) return annotationEntryShort

    // Look for star imports and make a guess.
    val hasStarImport = importPaths
        .filter { it.isAllUnder }
        .any {
            fqName.asString().startsWith(it.fqName.asString())
        }
    if (hasStarImport) return annotationEntryShort

    return null
}
