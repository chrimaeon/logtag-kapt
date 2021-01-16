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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
    ktlint
    id("com.jfrog.bintray") version "com.jfrog.bintray:com.jfrog.bintray.gradle.plugin".version()
}

repositories {
    mavenCentral()
    jcenter()
}

val functionalTestName = "functionalTest"

configurations {
    create("${functionalTestName}Implementation") {
        extendsFrom(testImplementation.get())
    }

    create("${functionalTestName}Runtime") {
        extendsFrom(testRuntime.get())
    }
}

sourceSets {
    create(functionalTestName) {
        java {
            srcDir("src/$functionalTestName/kotlin")
        }

        resources {
            srcDir("src/$functionalTestName/resources")
            outputDir = file("$buildDir/resources/$functionalTestName")
        }

        compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
        runtimeClasspath += output + compileClasspath
    }
}

idea {
    module {
        testSourceDirs = testSourceDirs + sourceSets[functionalTestName].allJava.srcDirs
        testResourceDirs = testResourceDirs + sourceSets[functionalTestName].resources.srcDirs
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    test {
        useJUnitPlatform()
        logEvents()
    }

    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = sourceSets[functionalTestName].output.classesDirs
        classpath = sourceSets[functionalTestName].runtimeClasspath
        logEvents()
        useJUnitPlatform()
    }

    check {
        dependsOn(functionalTest)
    }

    jar {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Built-By" to System.getProperty("user.name"),
                "Built-Date" to Date(),
                "Built-JDK" to System.getProperty("java.version"),
                "Built-Gradle" to gradle.gradleVersion
            )
        }
    }
}

fun Test.logEvents() = testLogging {
    events("PASSED", "SKIPPED", "FAILED")
}

val group: String by project
val versionName: String by project
val artifactId: String by project

project.group = group
project.version = versionName

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("processor") {

            from(components["java"])
            artifact(sourcesJar.get())

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
    val propsFile = file("${project.rootDir}/credentials.properties")

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

dependencies {
    addProcessorDependencies()
}
