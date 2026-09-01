/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag
value class MyValueClass(
    private val value: String,
) {
    fun log(): String = LOG_TAG
}

fun box(): String {
    val result = MyValueClass("Hello, World!").log()
    return if (result == "MyValueClass") {
        "OK"
    } else {
        "Failure: $result"
    }
}
