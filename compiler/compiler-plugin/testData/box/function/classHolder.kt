/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

class Holder {
    @LogTag
    fun memberTag(): String = LOG_TAG
}

fun box(): String {
    val result = Holder().memberTag()
    return if (result == "memberTag") {
        "OK"
    } else {
        "FAIL"
    }
}
