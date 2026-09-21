/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.LogTag

@LogTag
class ThisIsAVeryLongClassNameAndWillBeTruncated {
    fun log(message: String) {
        println("$LOG_TAG -> $message")
    }

    fun getLogTag(): String = LOG_TAG
}
