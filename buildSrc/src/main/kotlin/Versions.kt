// Generated with dependencyUpdates on 1/2/22, 3:44 PM
// DO NOT EDIT
@file:Suppress(
    "SpellCheckingInspection",
    "RedundantVisibilityModifier"
)

import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.collections.mapOf
import kotlin.error

private val versions: Map<String, String> = mapOf(
    "com.android.tools.build:gradle" to "7.0.4",
    "com.android.tools.lint:lint" to "30.0.4",
    "com.android.tools.lint:lint-api" to "30.0.4",
    "com.android.tools.lint:lint-checks" to "30.0.4",
    "com.android.tools.lint:lint-gradle" to "30.0.4",
    "com.android.tools.lint:lint-tests" to "30.0.4",
    "com.android.tools:testutils" to "30.0.4",
    "com.cmgapps.gradle:gradle-dependencies-versions-plugin" to "1.9.0",
    "com.github.tschuchortdev:kotlin-compile-testing-ksp" to "1.4.7",
    "com.google.auto.service:auto-service" to "1.0.1",
    "com.google.auto.service:auto-service-annotations" to "1.0.1",
    "com.google.devtools.ksp:symbol-processing" to "1.6.10-1.0.2",
    "com.google.devtools.ksp:symbol-processing-api" to "1.6.10-1.0.2",
    "com.pinterest:ktlint" to "0.43.2",
    "com.squareup:javapoet" to "1.13.0",
    "com.squareup:kotlinpoet" to "1.10.2",
    "net.ltgt.gradle.incap:incap" to "0.3",
    "net.ltgt.gradle.incap:incap-processor" to "0.3",
    "org.hamcrest:hamcrest" to "2.2",
    "org.jacoco:org.jacoco.ant" to "0.8.7",
    "org.jetbrains.dokka:dokka-base" to "1.6.10",
    "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin" to "1.6.10",
    "org.junit:junit-bom" to "5.8.2",
    "org.mockito.kotlin:mockito-kotlin" to  "4.0.0",
    "org.mockito:mockito-junit-jupiter" to  "4.2.0",
)


public fun String.version(): String = versions[this] ?: error("""No version found for $this""")

public fun String.withVersion(): String = "$this:${this.version()}"

public const val GRADLE_VERSION: String = "7.3.3"
