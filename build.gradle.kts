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
    id("com.cmgapps.publish") apply false
}

allprojects {
    val group = providers.gradleProperty("group")
    val versionName = providers.gradleProperty("versionName")

    this.group = group.get()
    this.version = versionName.get()
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
            val projectVersion = project.version

            inputs.property("libVersion", projectVersion)
            outputs.file(readmeFile)

            doLast {
                val content = readmeFile.readText()
                val oldVersion =
                    """id\("com.cmgapps.logtag"\) version "(.*)"""".toRegex(RegexOption.MULTILINE).find(content)?.let {
                        it.groupValues[1]
                    } ?: error("Cannot find oldVersion")

                val newContent = content.replace(oldVersion, projectVersion as String)
                readmeFile.writeText(newContent)
            }
        }

    patchChangelog {
        finalizedBy(updateReadme)
    }
}
