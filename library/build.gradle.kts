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
    `java-library`
    // STOPSHIP
   // id("com.android.library")
   ktlint
    `maven-publish`
    signing
}

// android {
//     compileSdkVersion(30)
//     defaultConfig {
//         minSdkVersion(15)
//         targetSdkVersion(30)
//     }
//     compileOptions {
//         sourceCompatibility = JavaVersion.VERSION_1_8
//         targetCompatibility = JavaVersion.VERSION_1_8
//     }
//
//     buildFeatures {
//         buildConfig = false
//     }
// }

dependencies {
    api(project(":runtime"))
    // STOPSHIP
    // lintPublish(project(":linter"))
}

// val sourcesJar by tasks.registering(Jar::class) {
//     archiveClassifier.set("sources")
//     from(android.sourceSets["main"].java.srcDirs)
// }
//
// val androidJavadocs by tasks.registering(Javadoc::class) {
//     source = android.sourceSets["main"].java.getSourceFiles()
//     classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
//     android.libraryVariants.forEach { variant ->
//         if (variant.name == "release") {
//             classpath += variant.javaCompileProvider.get().classpath
//         }
//     }
//     exclude("**/R.html", "**/R.*.html", "**/index.html")
// }
//
// val androidJavadocsJar by tasks.registering(Jar::class) {
//     archiveClassifier.set("javadoc")
//     from(androidJavadocs)
// }

val pubName = "library"

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>(pubName) {
               // from(components["release"])
               //  artifact(sourcesJar.get())
                // artifact(androidJavadocsJar.get())
            }
        }
    }

    signing {
        sign(publishing.publications[pubName])
    }
}
