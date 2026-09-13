/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import org.gradle.api.provider.Property

public interface LogTagExtension {
    public val enabled: Property<Boolean>
}

@Suppress("kotlin:S6526")
internal abstract class LogTagExtensionInternal : LogTagExtension {
    abstract val minSdk: Property<Int>

    init {
        enabled.convention(true)
    }
}
