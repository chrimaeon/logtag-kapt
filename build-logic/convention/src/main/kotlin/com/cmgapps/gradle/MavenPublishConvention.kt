/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension

@Suppress("unused")
class MavenPublishConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            plugins.apply(MavenPublishPlugin::class.java)

            extensions.configure(MavenPublishBaseExtension::class.java) { extension ->
                extension.publishToMavenCentral(
                    automaticRelease = false,
                )

                extension.signAllPublications()

                extension.coordinates(
                    artifactId = target.findProperty("artifactId") as? String,
                )

                extension.pom { pom ->
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

            extensions.configure(PublishingExtension::class.java) {
                it.repositories.maven {
                    it.name = "localStaging"
                    it.url = uri(rootProject.layout.buildDirectory.dir("local-staging"))
                }
            }
        }
    }
}
