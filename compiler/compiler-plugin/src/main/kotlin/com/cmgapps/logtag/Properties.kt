/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object LogTagPluginKey : GeneratedDeclarationKey() {
    override fun toString(): String = "FirLogTag"
}

val LOG_TAG_ANNOTATION_FQ_NAME = FqName("com.cmgapps.LogTag")
val LOG_TAG_PROPERTY_NAME = Name.identifier("LOG_TAG")
