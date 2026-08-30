/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

// RUN_PIPELINE_TILL: FRONTEND

package foo.bar

import com.cmgapps.LogTag

@LogTag
fun <!LogTagOnNonClass!>topLevelFunction<!>(): String = <!UNRESOLVED_REFERENCE!>LOG_TAG<!>
