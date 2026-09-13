/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag
import kotlin.jvm.JvmInline

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

@LogTag
enum class Works {
    VALUE1,
    VALUE2,
    ;

    fun log() = println("$LOG_TAG -> $name")
}

@JvmInline
@LogTag
value class ValueClass(
    private val value: Int,
) : Logger {
    override fun log(message: String) = println("$LOG_TAG:$value -> $message")
}

@Suppress("ktlint:standard:function-naming")
@LogTag
fun Test() {
    println("$LOG_TAG -> annotated function")
}

// class LogTagNotInScope {
//    fun logTag() = LOG_TAG
// }

fun main() {
    listOf(
        Public(),
        Internal(),
        Private(unused = "unused"),
        ThisIsAClassThatWillBeTruncated(),
        ThisIsAClassWithACustomLogTag(),
        Plain(),
        ClassWithCompanion(),
        ValueClass(42),
    ).forEach { it.log("Hello, World!") }
    Works.entries.forEach { it.log() }

    Test()
}
