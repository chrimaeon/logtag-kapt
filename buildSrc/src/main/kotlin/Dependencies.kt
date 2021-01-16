import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

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

private const val lintVersion = "27.1.1"

private val autoService = "com.google.auto.service:auto-service".withVersion()
private val autoServiceAnnotations =
    "com.google.auto.service:auto-service-annotations:" + "com.google.auto.service:auto-service".version()
private val hamcrest = "org.hamcrest:hamcrest".withVersion()
private val inCap = "net.ltgt.gradle.incap:incap:" + "net.ltgt.gradle.incap:incap-processor".version()
private val inCapProcessor = "net.ltgt.gradle.incap:incap-processor".withVersion()
private val javaPoet = "com.squareup:javapoet".withVersion()
private val jUnitBom = "org.junit:junit-bom".withVersion()
private const val jUnitJupiter = "org.junit.jupiter:junit-jupiter"
private val kotlinCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing".withVersion()
private val kotlinPoet = "com.squareup:kotlinpoet".withVersion()
private val lint = "com.android.tools.lint:lint".withVersion()
private const val lintApi = "com.android.tools.lint:lint-api:$lintVersion"
private const val lintChecks = "com.android.tools.lint:lint-checks:$lintVersion"
private val lintTests = "com.android.tools.lint:lint-tests".withVersion()
private val mockitoJupiter = "org.mockito:mockito-junit-jupiter".withVersion()
private val testUtils = "com.android.tools:testutils".withVersion()

fun DependencyHandlerScope.addProcessorDependencies() {
    implementation(project(":annotation"))

    implementation(kotlin("stdlib", KOTLIN_VERSION))
    implementation(kotlinPoet)
    implementation(javaPoet)

    compileOnly(autoServiceAnnotations)
    kapt(autoService)

    compileOnly(inCap)
    kapt(inCapProcessor)

    testImplementation(platform(jUnitBom))
    testImplementation(jUnitJupiter)
    testImplementation(mockitoJupiter)
    testImplementation(hamcrest)

    "functionalTestImplementation"(platform(jUnitBom))
    "functionalTestImplementation"(jUnitJupiter)
    "functionalTestImplementation"(kotlinCompileTesting)
}

fun DependencyHandlerScope.addLinterDependencies() {
    compileOnly(kotlin("stdlib", KOTLIN_VERSION))

    compileOnly(lintApi)
    compileOnly(lintChecks)

    compileOnly(autoServiceAnnotations)
    kapt(autoService)

    testImplementation(platform(jUnitBom))
    testImplementation(jUnitJupiter)

    testImplementation(lint)
    testImplementation(lintTests)
    testImplementation(testUtils)
}
