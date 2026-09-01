/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
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

package com.cmgapps.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.plugins.signing.SigningExtension

internal fun Project.configurePublish() {
    val group: String = project.findProperty("group") as String
    val versionName: String = project.findProperty("version") as String

    project.group = group
    project.version = versionName

    extensions.configure<PublishingExtension> {
        publications {
            register("libs", MavenPublication::class.java) {
                if (plugins.hasPlugin("com.android.library")) {
                    afterEvaluate {
                        from(components["release"])
                    }
                } else {
                    from(components["java"])
                }

                pom {
                    val artifactId: String = project.findProperty("artifactId") as String
                    val name: String = project.findProperty("name") as String
                    val description: String = project.findProperty("description") as String
                    val scmUrl: String = project.findProperty("scmUrl") as String
                    val connectionUrl: String = project.findProperty("connectionUrl") as String
                    val developerConnectionUrl: String = project.findProperty("developerConnectionUrl") as String
                    val projectUrl: String = project.findProperty("projectUrl") as String

                    groupId = project.group.toString()
                    setArtifactId(artifactId)
                    version = project.version.toString()

                    this.name.set(name)
                    this.description.set(description)
                    this.url.set(projectUrl)
                    developers {
                        developer {
                            this.id.set("chrimaeon")
                            this.name.set("Christian Grach")
                            this.email.set("christian.grach@cmgapps.com")
                        }
                    }

                    scm {
                        this.url.set(scmUrl)
                        this.connection.set(connectionUrl)
                        this.developerConnection.set(developerConnectionUrl)
                    }

                    issueManagement {
                        this.url.set("$projectUrl/issues")
                        this.system.set("github")
                    }

                    licenses {
                        license {
                            this.name.set("Apache-2.0")
                            this.url.set("https://spdx.org/licenses/Apache-2.0.html")
                        }
                    }
                }
            }

            extensions.configure<SigningExtension> {
                isRequired = !versionName.endsWith("SNAPSHOT")
                sign(publications)
            }
        }

        repositories {
            // TODO setup central sonatype
            mavenLocal()
        }
    }
}
