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

package com.cmgapps.kotlin.internal

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.net.URI
import javax.annotation.processing.Filer
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.NestingKind
import javax.tools.FileObject
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

internal class TestFiler : Filer {
    private var fileObject: FileObject? = null

    override fun createSourceFile(name: CharSequence?, vararg originatingElements: Element?): JavaFileObject {
        return TestFileObject(name.toString()).also {
            fileObject = it
        }
    }

    override fun createClassFile(name: CharSequence?, vararg originatingElements: Element?): JavaFileObject {
        throw UnsupportedOperationException()
    }

    override fun createResource(
        location: JavaFileManager.Location?,
        pkg: CharSequence?,
        relativeName: CharSequence?,
        vararg originatingElements: Element?
    ): FileObject = TestFileObject("$pkg.$relativeName").also {
        fileObject = it
    }

    override fun getResource(
        location: JavaFileManager.Location?,
        pkg: CharSequence?,
        relativeName: CharSequence?
    ): FileObject {
        throw UnsupportedOperationException()
    }

    fun getFileObject(): FileObject? = fileObject
}

internal class TestFileObject(private val _name: String) : JavaFileObject {

    private var bos: ByteArrayOutputStream? = null

    override fun toUri(): URI {
        throw UnsupportedOperationException()
    }

    override fun getName() = _name

    override fun openInputStream(): InputStream {
        throw UnsupportedOperationException()
    }

    override fun openOutputStream(): OutputStream = ByteArrayOutputStream().also {
        bos = it
    }

    override fun openReader(ignoreEncodingErrors: Boolean): Reader {
        throw UnsupportedOperationException()
    }

    override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence? {
        return bos?.toString(Charsets.UTF_8.name())
    }

    override fun openWriter(): Writer = OutputStreamWriter(openOutputStream())

    override fun getLastModified(): Long {
        throw UnsupportedOperationException()
    }

    override fun delete(): Boolean = true

    override fun getKind(): JavaFileObject.Kind = JavaFileObject.Kind.CLASS

    override fun isNameCompatible(simpleName: String?, kind: JavaFileObject.Kind?): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getNestingKind(): NestingKind {
        throw UnsupportedOperationException()
    }

    override fun getAccessLevel(): Modifier {
        throw UnsupportedOperationException()
    }
}
