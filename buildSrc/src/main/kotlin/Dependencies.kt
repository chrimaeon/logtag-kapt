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

import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

private val autoService = "com.google.auto.service:auto-service".withVersion()
private val autoServiceAnnotations =
    "com.google.auto.service:auto-service-annotations".withVersion()

private fun DependencyHandlerScope.junit(configurationName: String = "testImplementation") {
    configurationName(platform("org.junit:junit-bom".withVersion()))
    configurationName("org.junit.jupiter:junit-jupiter")
}

fun DependencyHandlerScope.addProcessorDependencies() {
    implementation(project(":annotation"))

    compileOnly("com.google.devtools.ksp:symbol-processing-api".withVersion())

    implementation(kotlin("stdlib-jdk8", KOTLIN_VERSION))
    compileOnly(kotlin("reflect", KOTLIN_VERSION))

    implementation("com.squareup:kotlinpoet".withVersion())
    implementation("com.squareup:javapoet".withVersion())

    compileOnly(autoServiceAnnotations)
    kapt(autoService)

    compileOnly("net.ltgt.gradle.incap:incap".withVersion())
    kapt("net.ltgt.gradle.incap:incap-processor".withVersion())

    junit()
    testImplementation("org.mockito:mockito-junit-jupiter".withVersion())
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.hamcrest:hamcrest".withVersion())

    junit("functionalTestImplementation")
    "functionalTestImplementation"(kotlin("reflect", KOTLIN_VERSION))
    "functionalTestImplementation"("com.github.tschuchortdev:kotlin-compile-testing-ksp".withVersion())
    "functionalTestImplementation"("com.google.devtools.ksp:symbol-processing-api".withVersion())
    "functionalTestImplementation"("com.google.devtools.ksp:symbol-processing".withVersion())
    "functionalTestImplementation"("org.jetbrains.kotlin:kotlin-compiler-embeddable:$KOTLIN_VERSION")
}

fun DependencyHandlerScope.addLinterDependencies() {
    compileOnly(kotlin("stdlib-jdk8", KOTLIN_VERSION))
    compileOnly(kotlin("reflect", KOTLIN_VERSION))

    compileOnly("com.android.tools.lint:lint-api".withVersion())
    compileOnly("com.android.tools.lint:lint-checks".withVersion())

    compileOnly(autoServiceAnnotations)
    kapt(autoService)

    junit()
    testImplementation("com.android.tools.lint:lint".withVersion())
    testImplementation("com.android.tools.lint:lint-tests".withVersion())
    testImplementation("com.android.tools:testutils".withVersion())
}
