/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.lint

import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.detector.api.CURRENT_API
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IssueRegistryShould {

    private lateinit var registry: IssueRegistry

    @Suppress("UnstableApiUsage")
    @BeforeEach
    fun setUp() {
        LintClient.clientName = LintClient.CLIENT_UNIT_TESTS
        registry = IssueRegistry()
    }

    @Test
    fun `have API version`() {
        assertThat(registry.api, `is`(CURRENT_API))
    }

    @Test
    fun `have issues registered`() {
        assertThat(registry.issues, contains(LogTagDetector.ISSUE))
    }

    @Test
    fun `have a vendor`() {
        assertThat(
            registry.vendor,
            allOf(
                hasProperty("vendorName", `is`("CMG Mobile Apps")),
                hasProperty("identifier", `is`("log-tag")),
                hasProperty("feedbackUrl", `is`("https://github.com/chrimaeon/logtag-kapt/issues")),
                hasProperty("contact", `is`("https://github.com/chrimaeon/logtag-kapt/issues"))
            )
        )
    }
}
