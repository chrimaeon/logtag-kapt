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

import com.jfrog.bintray.gradle.BintrayExtension
import java.util.Date
import java.util.Properties

plugins {
    id("com.android.library")
    ktlint
    `maven-publish`
    id("com.jfrog.bintray") version "com.jfrog.bintray:com.jfrog.bintray.gradle.plugin".version()
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(30)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    api(project(":annotation"))
    lintPublish(project(":linter"))
}

val group: String by project
val versionName: String by project
val artifactId: String by project

project.group = group
project.version = versionName

val name: String by project
val description: String by project

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("annotation") {
                from(components["release"])

                val name: String by project
                val description: String by project
                val scmUrl: String by project
                val connectionUrl: String by project
                val developerConnectionUrl: String by project

                this.groupId = project.group.toString()
                this.artifactId = artifactId
                this.version = project.version.toString()

                pom {
                    this.name.set(name)
                    this.description.set(description)
                    developers {
                        developer {
                            this.id.set("cgrach")
                            this.name.set("Christian Grach")
                        }
                    }

                    scm {
                        this.url.set(scmUrl)
                        this.connection.set(connectionUrl)
                        this.developerConnection.set(developerConnectionUrl)
                    }
                }
            }
        }
    }

    bintray {
        val credentialProps = Properties()
        val propsFile = project.rootDir.resolve("credentials.properties")

        if (propsFile.exists()) {
            credentialProps.load(propsFile.inputStream())
            user = credentialProps.getProperty("user")
            key = credentialProps.getProperty("key")
        }

        setPublications("processor")

        pkg(closureOf<BintrayExtension.PackageConfig> {
            repo = "maven"
            name = "${project.group}:$artifactId"
            userOrg = user
            setLicenses("Apache-2.0")
            val projectUrl: String by project
            vcsUrl = projectUrl
            val issuesTrackerUrl: String by project
            issueTrackerUrl = issuesTrackerUrl
            githubRepo = projectUrl
            version(closureOf<BintrayExtension.VersionConfig> {
                name = versionName
                vcsTag = versionName
                released = Date().toString()
            })
        })
    }
}
