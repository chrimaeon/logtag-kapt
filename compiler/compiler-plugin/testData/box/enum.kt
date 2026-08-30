/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag
enum class MyEnum {
    VALUE1,
    VALUE2,
    ;

    final fun log() = "$LOG_TAG $name"
}

fun box(): String {
    val results =
        MyEnum.entries.map {
            val result = it.log()
            result == "MyEnum ${it.name}"
        }

    return if (results.any { !it }) {
        "Fail: $results"
    } else {
        "OK"
    }
}
