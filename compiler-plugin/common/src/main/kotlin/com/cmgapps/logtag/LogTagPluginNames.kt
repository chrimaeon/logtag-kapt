/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object LogTagPluginNames {
    const val GROUP_ID = "com.cmgapps.logtag"
    const val PLUGIN_ID = "com.cmgapps.logtag"
    const val ENABLED_OPTION_NAME = "enabled"

    val LOG_TAG_ANNOTATION_ID = ClassId(FqName("com.cmgapps"), Name.identifier("LogTag"))
    val LOG_TAG_PROPERTY_NAME = Name.identifier("LOG_TAG")
}
