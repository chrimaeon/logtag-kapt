/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("kapt") version libs.versions.kotlin.get()
    id("com.android.lint")
    id("ktlint")
    alias(libs.plugins.dokka)
}

val buildConfigDirPath = layout.buildDirectory.dir("generated/source/buildConfig")

sourceSets {
    main {
        java.srcDir(buildConfigDirPath)
    }
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    val generateBuildConfig by registering {
        val outputDir = buildConfigDirPath

        val projectArtifactId = "log-tag"
        inputs.property("projectArtifactId", projectArtifactId)

        val issuesTrackerUrl: String by project
        inputs.property("issuesTrackerUrl", issuesTrackerUrl)

        val packageName = "com.cmgapps.lint"
        inputs.property("packageName", packageName)

        outputs.dir(outputDir)

        doLast {
            outputDir.get().asFile.mkdirs()
            file(outputDir.get().asFile.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val ISSUES_TRACKER_URL = "$issuesTrackerUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin(),
                )
            }
        }
    }

    withType<KotlinCompile> {
        dependsOn(generateBuildConfig)
    }

    jar {
        manifest {
            attributes("Lint-Registry-v2" to "com.cmgapps.lint.IssueRegistry")
        }
    }

    // koverVerify {
    //     rule {
    //         name = "Minimal line coverage"
    //         bound {
    //             minValue = 80
    //             valueType = kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE
    //         }
    //     }
    // }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.lint.api)
    compileOnly(libs.android.lint.checks)

    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice.autoservice)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.hamcrest)
    testImplementation(libs.android.lint.lint)
    testImplementation(libs.android.lint.test)
    testImplementation(libs.android.testutils)
}
