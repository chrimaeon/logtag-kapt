/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.signing.SigningExtension

@Suppress("unused")
class PublishConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target.plugins) {
            apply("maven-publish")
            apply("signing")
        }

        val group: String = target.findProperty("group") as String
        val versionName: String = target.findProperty("version") as String

        target.group = group
        target.version = versionName

        target.extensions.configure(PublishingExtension::class.java) { extension ->
            extension.publications { container ->
                val libs =
                    container.register("libs", MavenPublication::class.java) { maven ->
                        if (target.plugins.hasPlugin("com.android.library")) {
                            target.afterEvaluate {
                                maven.from(it.components.getByName("release"))
                            }
                        } else {
                            maven.from(target.components.getByName("java"))
                        }

                        maven.artifactId = target.findProperty("artifactId") as String

                        maven.pom { pom ->
                            val name: String = target.findProperty("name") as String
                            val description: String = target.findProperty("description") as String
                            val scmUrl: String = target.findProperty("scmUrl") as String
                            val connectionUrl: String = target.findProperty("connectionUrl") as String
                            val developerConnectionUrl: String = target.findProperty("developerConnectionUrl") as String
                            val projectUrl: String = target.findProperty("projectUrl") as String

                            pom.name.set(name)
                            pom.description.set(description)
                            pom.url.set(projectUrl)
                            pom.developers { devSpec ->
                                devSpec.developer {
                                    it.id.set("chrimaeon")
                                    it.name.set("Christian Grach")
                                    it.email.set("christian.grach@cmgapps.com")
                                }
                            }

                            pom.scm { scm ->
                                scm.url.set(scmUrl)
                                scm.connection.set(connectionUrl)
                                scm.developerConnection.set(developerConnectionUrl)
                            }

                            pom.issueManagement { issues ->
                                issues.url.set("$projectUrl/issues")
                                issues.system.set("github")
                            }

                            pom.licenses { licenseSpec ->
                                licenseSpec.license {
                                    it.name.set("Apache-2.0")
                                    it.url.set("https://spdx.org/licenses/Apache-2.0.html")
                                }
                            }
                        }
                    }

                target.extensions.configure(SigningExtension::class.java) {
                    it.isRequired = !versionName.endsWith("SNAPSHOT")
                    it.sign(libs.get())
                }
            }

            extension.repositories {
                // TODO setup central sonatype
                it.mavenLocal()
            }
        }
    }
}
