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

plugins {
    id("com.android.library")
    `maven-publish`
    signing
}

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 15
        targetSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = false
    }
}

dependencies {
    api(project(":annotation"))
    lintPublish(project(":linter"))
}

val group: String by project
val versionName: String by project

project.group = group
project.version = versionName

val name: String by project
val description: String by project

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(projectDir.resolve("README.md"))
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(projectDir.resolve("README.md"))
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("libraryMaven") {
                from(components["release"])
                artifact(sourcesJar)
                artifact(javadocJar)
                logtagPom(project)
            }
        }

        repositories {
            sonatype(project)
        }
    }

    signing {
        sign(publishing.publications["libraryMaven"])
    }
}
