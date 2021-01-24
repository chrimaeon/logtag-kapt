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

import com.cmgapps.LogTag
import timber.log.Timber
import test.java.pkg.JavaTestClass

interface Log {
    fun log()
}

@LogTag("CustomTag")
class TestClass : Log {
    override fun log() {
        Timber.i("info message")
        Timber.tag("Custom").e("error message")
    }
}

// @LogTag
internal class InternalTestClass : Log {
    override fun log() {
        Timber.d("debug message")
    }
}

@LogTag
class TestClassWithAFarTooLongNameForLogging : Log {
    override fun log() {
        Timber.d("no tag debug message")
    }
}

fun main() {
    Timber.plant(TimberTree());
    val loggable = listOf<Log>(TestClass(), InternalTestClass(), TestClassWithAFarTooLongNameForLogging(), JavaTestClass())
    loggable.forEach {
        it.log()
    }
}

private class TimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        println("$priority/$tag: $message")
    }
}
