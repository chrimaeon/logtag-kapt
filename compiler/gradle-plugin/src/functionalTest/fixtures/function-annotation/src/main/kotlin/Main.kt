/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.LogTag

@LogTag
fun getLogTag(): String = LOG_TAG

fun main() {
    val logTag = getLogTag()
    println("$logTag -> Hello World")
}
