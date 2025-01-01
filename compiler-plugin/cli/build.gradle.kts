import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Properties

/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("jvm")
    kotlin("kapt")
    // Cannot use convention due to shadowJar
    // id("com.cmgapps.publish")
    `maven-publish`
    signing
    alias(libs.plugins.shadow)
}

val group: String by project
val versionName: String by project

project.group = group
project.version = versionName

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register<MavenPublication>("libs") {
            from(components["shadow"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                val artifactId: String by project
                val name: String by project
                val description: String by project
                val scmUrl: String by project
                val connectionUrl: String by project
                val developerConnectionUrl: String by project
                val projectUrl: String by project

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
                        rootProject.file("credentials.properties").inputStream().use {
                            load(it)
                        }
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

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

dependencies {
    api(projects.compilerPlugin.common)
    api(projects.compilerPlugin.backend)
    api(projects.compilerPlugin.k2)
    compileOnly(libs.jetbrains.compiler.embeddable)
    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice.autoservice)

    testImplementation(libs.jetbrains.compiler.embeddable)
    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
}
