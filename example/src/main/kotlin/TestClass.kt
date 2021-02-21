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

package test.pkg

import com.cmgapps.logtag.annotation.LogTag as LogTagAnnotation
import com.cmgapps.logtag.LogTag
import timber.log.Timber
import test.java.pkg.JavaTestClass

interface Log {
    fun log()
}

@LogTagAnnotation("CustomTag")
class TestClass : LogTag, Log {
    override fun log() {
        timber.log.Timber.tag(LOG_TAG).i("${this::class.java.simpleName}: info message with LOG_TAG")
        Timber.tag("Custom").e("${this::class.java.simpleName}: error message with tag method")
        Timber.tag(logTag).i("${this::class.java.simpleName}: info message with logTag")
    }
}

internal class InternalTestClass : Log {
    override fun log() {
        Timber.d("${this::class.java.simpleName}: no tag should be applied")
    }
}

class TestClassWithAFarTooLongNameForLogging : LogTag, Log {
    override fun log() {
        Timber.tag(logTag).d("debug message with logTag and truncated tag")
    }
}

class TestDebugVerbose: LogTag, Log {
    override fun log() {
        Timber.tag(logTag).d("debug message with logTag and truncated tag")
        Timber.v("verbose message with logTag and truncated tag")
    }
}

fun main() {
    Timber.plant(TimberTree())
    val loggable =
        listOf<Log>(TestClass(), InternalTestClass(), TestClassWithAFarTooLongNameForLogging(), JavaTestClass(), TestDebugVerbose())
    println("BuildConfig.DEBUG = ${com.cmgapps.BuildConfig.DEBUG}")
    loggable.forEach {
        it.log()
    }
}

private class TimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("${priority.asPriorityString()}/$tag: $message")
    }

    private fun Int.asPriorityString() = when (this) {
        2 -> "VERBOSE"
        3 -> "DEBUG"
        4 -> "INFO"
        5 -> "WARN"
        6 -> "ERROR"
        7 -> "ASSERT"
        else -> "N/A"
    }
}
