/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package foo.bar

import com.cmgapps.LogTag

@LogTag("ClassTag")
class TaggedClass {
    fun classTag(): String = LOG_TAG

    @LogTag("FunctionTag")
    fun functionTag(): String = LOG_TAG
}

fun box(): String {
    val tagged = TaggedClass()
    return if (tagged.classTag() == "ClassTag" && tagged.functionTag() == "FunctionTag") "OK" else "FAIL"
}
