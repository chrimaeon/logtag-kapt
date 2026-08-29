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

package foo.bar

import com.cmgapps.LogTag

fun interface Logger {
    fun log(message: String): Unit
}

@LogTag
class Public : Logger {
    override fun log(message: String) {
        println("$LOG_TAG -> $message")
    }
}

@LogTag
internal class Internal : Logger {
    override fun log(message: String) {
        println("$LOG_TAG -> $message")
    }
}

@LogTag
class ThisIsAClassThatWillBeTruncated : Logger {
    override fun log(message: String) {
        println("$LOG_TAG -> $message")
    }
}

@LogTag("ShortTag")
class ThisIsAClassWithACustomLogTag : Logger {
    override fun log(message: String) = println("$LOG_TAG -> $message")
}

@LogTag
fun tagging(): String = ""

@LogTag("PRIVATE")
private data class Private(
    private val unused: String,
) : Logger {
    override fun log(message: String) {
        println("$LOG_TAG:$unused -> $message")
    }
}

@LogTag
class ClassWithCompanion : Logger {
    override fun log(message: String) {
        println("$LOG_TAG -> $message")
        println("$MY_TAG -> $message")
    }

    companion object {
        private const val MY_TAG = "My Log Tag"
    }
}

class Plain : Logger {
    override fun log(message: String) = println("$LOG_TAG -> $message")

    companion object {
        private const val LOG_TAG = "Plain"
    }
}

enum class Works {
    VALUE1,
    VALUE2,
}

@androidx.compose.runtime.Composable
@LogTag
fun Test() {
}

@LogTag
fun wontWorkTest() {
}

fun main() {
    listOf(
        Public(),
        Internal(),
        Private(unused = "unused"),
        ThisIsAClassThatWillBeTruncated(),
        ThisIsAClassWithACustomLogTag(),
        Plain(),
        ClassWithCompanion(),
    ).forEach {
        it.log("Hello, World!")
    }

//    println("@Composable Test -> ${ComposableTest.LOG_TAG}")
}
