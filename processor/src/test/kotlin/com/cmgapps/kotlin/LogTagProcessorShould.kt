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

import cmgapps.LogTag
import com.cmgapps.kotlin.internal.TestFiler
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.io.File
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.Name
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@ExtendWith(MockitoExtension::class)
class LogTagProcessorShould {

    private lateinit var processor: LogTagProcessor
    private lateinit var filer: TestFiler

    @TempDir
    lateinit var tempDir: File

    @Mock
    private lateinit var environmentMock: ProcessingEnvironment

    @Mock
    private lateinit var roundEnvironmentMock: RoundEnvironment

    @Mock
    private lateinit var messagerMock: Messager

    @Mock
    private lateinit var elementUtilsMock: Elements

    @BeforeEach
    fun before() {
        filer = TestFiler()
        `when`(environmentMock.filer).thenReturn(filer)
        `when`(environmentMock.messager).thenReturn(messagerMock)
        `when`(environmentMock.options).thenReturn(mapOf("kapt.kotlin.generated" to tempDir.absolutePath))

        processor = LogTagProcessor().apply {
            init(environmentMock)
        }
    }

    @Test
    fun `generate code for valid kotlin annotation`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java)
        `when`(logTagAnnotationMock.value).thenReturn("")

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClass".asName())
        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(mock(Metadata::class.java))
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)

        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        @Language("Kt")
        val expected = """
            // Automatically generated file. DO NOT MODIFY
            @file:Suppress(
              "SpellCheckingInspection",
              "RedundantVisibilityModifier",
              "unused"
            )

            package test.pkg

            import kotlin.String
            import kotlin.Suppress

            public val TestClass.LOG_TAG: String
              inline get() = "TestClass"

        """.trimIndent()

        assertThat(tempDir.resolve("test/pkg/TestClassLogTag.kt").readText(), `is`(expected))
    }

    @Test
    fun `generate code for valid java annotation`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClass".asName())
        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(null)
        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java)
        `when`(logTagAnnotationMock.value).thenReturn("")
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)

        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        @Language("Java")
        val expected = """
            // Automatically generated file. DO NOT MODIFY
            package test.pkg;

            import java.lang.String;

            class TestClassLogTag {
              static final String LOG_TAG = "TestClass";
            }

        """.trimIndent()

        assertThat(filer.getFileObject()?.getCharContent(false), `is`(expected))
    }

    @Test
    fun `return true for valid annotation`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClass".asName())
        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(mock(Metadata::class.java))
        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java).also {
            `when`(it.value).thenReturn("")
        }
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)

        val result = processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        assertThat(result, `is`(true))
    }

    @Test
    fun `return false if empty annotations`() {

        val result = processor.process(emptySet<TypeElement>().toMutableSet(), roundEnvironmentMock)
        assertThat(result, `is`(false))
    }

    @Test
    fun `return false if not valid annotations`() {
        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn("com.test.Class".asName())

        val result = processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)
        assertThat(result, `is`(false))
    }

    @Test
    fun `log error if class is not valid`() {
        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.FIELD)
        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        verify(messagerMock).printMessage(
            Diagnostic.Kind.ERROR,
            "LogTag annotation can only be applied to a class/interface"
        )
    }

    @Test
    fun `log error if class is not public`() {
        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PRIVATE, Modifier.FINAL))
        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        verify(messagerMock).printMessage(
            Diagnostic.Kind.ERROR,
            "LogTag annotation can only be applied to public classes"
        )
    }

    @Test
    fun `truncate tag if class name is too long`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClassWithAFarTooLongName".asName())

        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(mock(Metadata::class.java))
        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java).also {
            `when`(it.value).thenReturn("")
        }
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)
        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        @Language("Kt")
        val expected = """
            // Automatically generated file. DO NOT MODIFY
            @file:Suppress(
              "SpellCheckingInspection",
              "RedundantVisibilityModifier",
              "unused"
            )

            package test.pkg

            import kotlin.String
            import kotlin.Suppress

            public val TestClassWithAFarTooLongName.LOG_TAG: String
              inline get() = "TestClassWithAFarTooLo…"

        """.trimIndent()

        assertThat(tempDir.resolve("test/pkg/TestClassWithAFarTooLongNameLogTag.kt").readText(), `is`(expected))
    }

    @Test
    fun `show warning if class name is too long`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClassWithAFarTooLongName".asName())
        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(mock(Metadata::class.java))
        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java).also {
            `when`(it.value).thenReturn("")
        }
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)

        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        verify(messagerMock).printMessage(
            Diagnostic.Kind.WARNING,
            "Class name \"TestClassWithAFarTooLongName\" exceeds max. length of 23 for a log tag. Class name will be truncated. Add the @com.cmgapps.LogTag annotation with a custom tag to override"
        )
    }

    @Test
    fun `use custom log tag`() {
        `when`(environmentMock.elementUtils).thenReturn(elementUtilsMock)
        `when`(elementUtilsMock.getPackageOf(any())).thenAnswer {
            it.getArgument<TypeElement>(0).enclosingElement
        }

        val annotationMock = mock(TypeElement::class.java)
        `when`(annotationMock.qualifiedName).thenReturn(cmgapps.LogTag::class.java.canonicalName.asName())

        val element = mock(TypeElement::class.java)
        `when`(element.kind).thenReturn(ElementKind.CLASS)
        `when`(element.modifiers).thenReturn(setOf(Modifier.PUBLIC))
        `when`(element.simpleName).thenReturn("TestClass".asName())
        val packageElement = mock(PackageElement::class.java).also {
            `when`(it.qualifiedName).thenReturn("test.pkg".asName())
        }
        `when`(element.enclosingElement).thenReturn(packageElement)

        `when`(roundEnvironmentMock.getElementsAnnotatedWith(any<Class<out Annotation>>())).thenReturn(
            mutableSetOf(
                element
            )
        )

        `when`(element.getAnnotation(Metadata::class.java)).thenReturn(mock(Metadata::class.java))
        val logTagAnnotationMock = mock(cmgapps.LogTag::class.java).also {
            `when`(it.value).thenReturn("MyCustomLogTag")
        }
        `when`(element.getAnnotation(cmgapps.LogTag::class.java)).thenReturn(logTagAnnotationMock)

        processor.process(mutableSetOf(annotationMock), roundEnvironmentMock)

        @Language("Kt")
        val expected = """
            // Automatically generated file. DO NOT MODIFY
            @file:Suppress(
              "SpellCheckingInspection",
              "RedundantVisibilityModifier",
              "unused"
            )

            package test.pkg

            import kotlin.String
            import kotlin.Suppress

            public val TestClass.LOG_TAG: String
              inline get() = "MyCustomLogTag"

        """.trimIndent()

        assertThat(tempDir.resolve("test/pkg/TestClassLogTag.kt").readText(), `is`(expected))
    }
}

private fun String.asName(): Name = object : Name {
    override fun get(index: Int): Char {
        return this@asName[index]
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return this@asName.subSequence(startIndex, endIndex)
    }

    override fun contentEquals(cs: CharSequence?): Boolean {
        return this@asName == cs
    }

    override val length: Int
        get() = this@asName.length

    override fun toString(): String {
        return this@asName
    }

    override fun equals(other: Any?): Boolean {
        return this@asName == other
    }
}
