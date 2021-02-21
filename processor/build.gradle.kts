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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

plugins {
    idea
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish`
    signing
    ktlint
    id("org.jetbrains.dokka") version "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin".version()
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
}

fun Test.logEvents() = testLogging {
    this.events("PASSED", "SKIPPED", "FAILED")
}

val group: String by project
val versionName: String by project

project.group = group
project.version = versionName

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

val pubName = "processor"

publishing {
    publications {
        create<MavenPublication>(pubName) {
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}

signing {
    sign(publishing.publications[pubName])
}

dependencies {
    addProcessorDependencies()
}
