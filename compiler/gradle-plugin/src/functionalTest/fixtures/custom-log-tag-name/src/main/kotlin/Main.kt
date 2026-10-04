/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.LogTag

@LogTag
class Main {
    fun log(message: String) {
        println("$CUSTOM_TAG -> $message")
    }

    fun getLogTag(): String = CUSTOM_TAG
}

fun main() {
    val result =
        Main().run {
            log("Hello World")
            getLogTag()
        }

    println(result)
}
