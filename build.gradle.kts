import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel

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

plugins {
    alias(libs.plugins.android.library) apply false
    kotlin("jvm") version libs.versions.kotlin.get() apply false
    kotlin("kapt") version libs.versions.kotlin.get() apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.jetbrains.changelog)
}

val versionName: String by project

project.version = versionName

changelog {
    header = provider { version.get() }
    repositoryUrl = providers.gradleProperty("projectUrl")
}

tasks {
    named<Wrapper>("wrapper") {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = libs.versions.gradle.get()
    }

    named<DependencyUpdatesTask>("dependencyUpdates") {
        revision = "release"
        gradleReleaseChannel = GradleReleaseChannel.CURRENT.id

        rejectVersionIf {
            listOf("alpha", "beta", "rc", "cr", "m", "eap").any { qualifier ->
                """(?i).*[.-]?$qualifier[.\d-]*"""
                    .toRegex()
                    .containsMatchIn(candidate.version)
            }
        }
    }

    val updateReadme by registering {
        val readmeFile = rootDir.resolve("README.md")

        inputs.property("libVersion", version)
        outputs.file(readmeFile)

        doLast {
            val content = readmeFile.readText()
            val oldVersion =
                """id\("com.cmgapps.logtag"\) version "(.*)"""".toRegex(RegexOption.MULTILINE).find(content)?.let {
                    it.groupValues[1]
                } ?: error("Cannot find oldVersion")

            logger.info("Updating README.md version $oldVersion to $version")

            val newContent = content.replace(oldVersion, version as String)
            readmeFile.writeText(newContent)
        }
    }

    patchChangelog {
        finalizedBy(updateReadme)
    }
}
