/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

fun log(
    tag: String,
    message: String,
): String = "$tag -> $message"

@LogTag
fun equals(
    first: Int,
    second: Int,
): Boolean {
    if (first == second) {
        return true
    }

    println(log(LOG_TAG, "not equal"))
    return false
}

fun box(): String {
    if (equals(1, 1)) {
        return "OK"
    } else {
        return "Fail"
    }
}
