/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag("ShortTag")
class ThisIsAClassWithACustomLogTag {
    fun log(message: String) = LOG_TAG
}

fun box(): String {
    val result = ThisIsAClassWithACustomLogTag().log("Hello World")

    return if (result == "ShortTag") {
        "OK"
    } else {
        "Fail: $result"
    }
}
