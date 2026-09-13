/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag
class ThisIsAClassThatWillNotBeTruncated {
    fun log(message: String) = LOG_TAG
}

fun box(): String {
    val result = ThisIsAClassThatWillNotBeTruncated().log("Hello World")

    return if (result == "ThisIsAClassThatWillNotBeTruncated") {
        "OK"
    } else {
        "Fail: $result"
    }
}
