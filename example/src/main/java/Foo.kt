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

@com.cmgapps.LogTag("Test")
class Public {
    fun logging(): String {
        return LOG_TAG
    }
}

@com.cmgapps.LogTag()
internal class Internal {
    fun logging(): String {
        return LOG_TAG
    }
}

@com.cmgapps.LogTag
class ThisIsAClassNameThatIsTooLong {
    fun logging(): String {
        return LOG_TAG
    }
}

@com.cmgapps.LogTag("ShortTag")
class ThisIsAClassNameThatIsTooLongWithTag {
    fun logging(): String {
        return LOG_TAG
    }
}

// @com.cmgapps.LogTag
fun tagging(): String {
    return ""
}

// @com.cmgapps.LogTag("ShortTag")
private class Private {
    fun logging(): String {
        return LOG_TAG
    }
}

fun main() {
    Public().run {
        println(this.logging())
    }

    Internal().run {
        println(this.logging())
    }

    ThisIsAClassNameThatIsTooLong().run {
        println(this.logging())
    }

    ThisIsAClassNameThatIsTooLongWithTag().run {
        println(this.logging())
    }

    FooJava().run {
        println(this.logging())
    }

    PackageJava().run {
        println(this.logging())
    }
}
