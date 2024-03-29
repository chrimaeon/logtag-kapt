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
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.android.lint")
    ktlint
    id("org.jetbrains.dokka") version "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin".version()
}

@OptIn(ExperimentalPathApi::class)
val buildConfigDirPath = buildDir.toPath() / "generated" / "source" / "buildConfig"

sourceSets {
    main {
        java.srcDir(buildConfigDirPath)
    }
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
            outputDir.toFile().mkdirs()
            file(outputDir.resolve("BuildConfig.kt")).bufferedWriter().use {
                it.write(
                    """
                        |package $packageName
                        |const val ISSUES_TRACKER_URL = "$issuesTrackerUrl"
                        |const val PROJECT_ARTIFACT = "$projectArtifactId"
                    """.trimMargin()
                )
            }
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
        dependsOn(generateBuildConfig)
    }

    jar {
        manifest {
            attributes("Lint-Registry-v2" to "com.cmgapps.lint.IssueRegistry")
        }
    }

    koverVerify {
        rule {
            name = "Minimal line coverage"
            bound {
                minValue = 80
                valueType = kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    addLinterDependencies()
}
