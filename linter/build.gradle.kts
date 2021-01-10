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

plugins {
    `java-library`
    id("com.android.lint")
    kotlin("jvm")
    kotlin("kapt")
    ktlint
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("PASSED", "SKIPPED", "FAILED")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    jar {
        manifest {
            attributes("Lint-Registry-v2" to "com.cmgapps.lint.IssueRegistry")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    compileOnly(kotlin("stdlib", KOTLIN_VERSION))

    compileOnly("com.android.tools.lint:lint-api:27.1.1")
    compileOnly("com.android.tools.lint:lint-checks:27.1.1")

    compileOnly("com.google.auto.service:auto-service".withVersion())
    kapt("com.google.auto.service:auto-service".withVersion())

    testImplementation(platform("org.junit:junit-bom".withVersion()))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("com.android.tools.lint:lint".withVersion())
    testImplementation("com.android.tools.lint:lint-tests".withVersion())
    testImplementation("com.android.tools:testutils".withVersion())
}
