/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.junit.Assert.assertEquals
import org.junit.Test

class FunctionAnnotationShould {
    @Test
    fun returnLogTag() {
        assertEquals("getLogTag", getLogTag())
    }
}
