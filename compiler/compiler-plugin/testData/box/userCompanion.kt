/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag
class Foo {
    fun log(message: String) = LOG_TAG

    companion object {
        const val MY_TAG = "MY_TAG"
    }
}

fun box(): String {
    val result = Foo().log("Hello World")

    if (Foo.MY_TAG != "MY_TAG") {
        return "Fail: Companion Tag different -> ${Foo.MY_TAG}"
    }

    if (result != "Foo") {
        return "Fail: $result"
    }

    return "OK"
}
