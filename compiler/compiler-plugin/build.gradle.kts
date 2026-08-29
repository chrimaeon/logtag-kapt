/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Properties

plugins {
    kotlin("jvm")
    alias(libs.plugins.buildconfig)
    `maven-publish`
    signing
}

val versionName: String = project.findProperty("versionName") as String
project.version = versionName

java {
    withSourcesJar()
    withJavadocJar()
}

buildConfig {
    packageName.set("com.cmgapps.logtag")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

publishing {
    publications {
        register<MavenPublication>("compiler-plugin") {
            from(components["java"])

            pom {
                val artifactId: String = project.name
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
                        this.name.set("Apache License, Version 2.0")
                        this.url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }

        signing {
            isRequired = !versionName.endsWith("SNAPSHOT")
            sign(publications)
        }

        repositories {
            maven {
                name = "sonatype"
                val releaseUrl = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotUrl = project.uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (versionName.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

                val credentialProperties =
                    Properties().apply {
                        rootProject.file("credentials.properties").inputStream().use(::load)
                    }

                val username: String by credentialProperties
                val password: String by credentialProperties

                credentials {
                    this.username = username
                    this.password = password
                }
            }
            mavenLocal()
        }
    }
}

dependencies {
    compileOnly(libs.jetbrains.kotlin.compiler)
}
