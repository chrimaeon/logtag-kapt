/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class Kover : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(KoverGradlePlugin::class.java)

            extensions.configure(KoverProjectExtension::class.java) { extension ->
                extension.useJacoco()

                extension.reports { reportsConfig ->
                    reportsConfig.verify { verify ->
                        verify.rule("Minimal line coverage") { rule ->
                            rule.bound { bound ->
                                bound.minValue.set(80)
                                bound.coverageUnits.set(CoverageUnit.LINE)
                                bound.aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
                            }
                        }
                    }

                    reportsConfig.total { total ->
                        total.log { logTaskConfig ->
                            logTaskConfig.onCheck.set(true)
                            logTaskConfig.header.set("Total Test Line Coverage")
                            logTaskConfig.groupBy.set(GroupingEntityType.APPLICATION)
                            logTaskConfig.aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
                            logTaskConfig.coverageUnits.set(CoverageUnit.LINE)
                            logTaskConfig.format.set("<value>% total line coverage")
                        }
                    }
                }
            }
        }
    }
}
