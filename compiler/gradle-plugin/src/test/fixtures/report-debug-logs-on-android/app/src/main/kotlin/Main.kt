/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import android.util.Log
import com.cmgapps.LogTag

@LogTag
class Main {
    fun log(message: String) {
        Log.d(LOG_TAG, message)
    }
}
