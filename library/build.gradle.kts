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
    id("com.cmgapps.publish")
}

android {
    namespace = "com.cmgapps.logtag"
    compileSdk = 37
    defaultConfig {
        minSdk = 15
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    api(project(":annotation"))
    lintPublish(project(":linter"))
}

val sourcesJar =
    tasks.register("sourcesJar", Jar::class) {
        description = "Generate an empty sources jar for the library."
        group = "docs"
        archiveClassifier.set("sources")
        from(projectDir.resolve("README.md"))
    }

val javadocJar =
    tasks.register("javadocJar", Jar::class) {
        description = "Generate an empty javadoc jar for the library."
        group = "docs"
        archiveClassifier.set("javadoc")
        from(projectDir.resolve("README.md"))
    }

publishing {
    publications {
        named<MavenPublication>("libs") {
            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }
}
