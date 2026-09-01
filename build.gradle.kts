/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.changelog)
}

allprojects {
    val versionName: String = project.findProperty("versionName") as String
    version = versionName
}

changelog {
    header = provider { version.get() }
    repositoryUrl = providers.gradleProperty("projectUrl")
}

tasks {
    named<Wrapper>("wrapper") {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = libs.versions.gradle.get()
    }

    val updateReadme =
        register("updateReadme") {
            description = "Update the Readme with the current version"
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
