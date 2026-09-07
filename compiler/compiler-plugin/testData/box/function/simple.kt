/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag
fun defaultTag(): String = LOG_TAG

fun box(): String {
    val result = defaultTag()
    return if (result == "defaultTag") {
        "OK"
    } else {
        "FAIL"
    }
}
