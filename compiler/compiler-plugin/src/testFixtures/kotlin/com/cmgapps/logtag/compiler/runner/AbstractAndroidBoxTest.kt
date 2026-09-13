/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.logtag.compiler.runner

import com.cmgapps.logtag.LogTagConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfigurationKey

open class AbstractAndroidBoxTest : AbstractJvmBoxTest() {
    override val configurationMap: Map<CompilerConfigurationKey<*>, Any> =
        mapOf(
            LogTagConfigurationKeys.ENABLED to true,
            LogTagConfigurationKeys.ANDROID_MIN_SDK to 21,
        )
}
