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

import com.cmgapps.gradle.VersionsExtension
import com.cmgapps.gradle.VersionsPlugin
import kotlinx.kover.api.CoverageEngine.JACOCO

buildscript {
    repositories {
        google()
        mavenCentral()
        helixTeamHubRepo {
            if (project.hasProperty("DEVEO_USERNAME")) {
                username = project.property("DEVEO_USERNAME") as String
                password = project.property("DEVEO_PASSWORD") as String
            } else {
                username = System.getenv("DEVEO_USERNAME")
                password = System.getenv("DEVEO_PASSWORD")
            }
        }
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle".withVersion())
        classpath(kotlin("gradle-plugin", KOTLIN_VERSION))
        classpath("com.cmgapps.gradle:gradle-dependencies-versions-plugin".withVersion())
    }
}

repositories {
    google()
    mavenCentral()
}

apply<VersionsPlugin>()

plugins {
    id("org.jetbrains.kotlinx.kover") version
        "org.jetbrains.kotlinx.kover:org.jetbrains.kotlinx.kover.gradle.plugin".version()
}

extensions.configure(VersionsExtension::class.java) {
    skipGroups.addAll("org.jetbrains.kotlin", "org.junit.jupiter", "org.jetbrains.intellij.deps", "org.jacoco")
}

kover {
    coverageEngine.set(JACOCO)
}

subprojects {
    repositories {
        google()
        mavenCentral()
    }

    gradle.projectsEvaluated {
        tasks.withType<JavaCompile>()
            .configureEach {
                options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xmaxerrs", "500"))
            }
    }
}

tasks {
    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    named<Wrapper>("wrapper") {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = GRADLE_VERSION
    }
}
