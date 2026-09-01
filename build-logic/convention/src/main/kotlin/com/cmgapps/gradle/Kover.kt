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

class Kover : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(KoverGradlePlugin::class.java)

            extensions.configure(KoverProjectExtension::class.java) {
                useJacoco()

                reports {
                    verify {
                        rule("Minimal line coverage") {
                            bound {
                                minValue.set(80)
                                coverageUnits.set(CoverageUnit.LINE)
                                aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
                            }
                        }
                    }

                    total {
                        log {
                            onCheck.set(true)
                            header.set("Total Test Line Coverage")
                            groupBy.set(GroupingEntityType.APPLICATION)
                            aggregationForGroup.set(AggregationType.COVERED_PERCENTAGE)
                            coverageUnits.set(CoverageUnit.LINE)
                            format.set("<value>% total line coverage")
                        }
                    }
                }
            }
        }
    }
}
