// Generated with dependencyUpdates on 6/3/21, 12:55 PM
// DO NOT EDIT
@file:Suppress(
    "SpellCheckingInspection",
    "RedundantVisibilityModifier"
)

private val versions: Map<String, String> = mapOf(
    "com.android.tools.build:gradle" to "4.2.1",
    "com.android.tools.lint:lint" to "27.2.1",
    "com.android.tools.lint:lint-api" to "27.2.1",
    "com.android.tools.lint:lint-checks" to "27.2.1",
    "com.android.tools.lint:lint-gradle" to "27.2.1",
    "com.android.tools.lint:lint-tests" to "27.2.1",
    "com.android.tools:testutils" to "27.2.1",
    "com.cmgapps.gradle:gradle-dependencies-versions-plugin" to "1.9.0",
    "com.github.tschuchortdev:kotlin-compile-testing-ksp" to "1.4.1",
    "com.google.auto.service:auto-service" to "1.0",
    "com.google.auto.service:auto-service-annotations" to "1.0",
    "com.google.devtools.ksp:symbol-processing" to "1.5.10-1.0.0-beta01",
    "com.google.devtools.ksp:symbol-processing-api" to "1.5.10-1.0.0-beta01",
    "com.pinterest:ktlint" to "0.41.0",
    "com.squareup:javapoet" to "1.13.0",
    "com.squareup:kotlinpoet" to "1.8.0",
    "net.ltgt.gradle.incap:incap" to "0.3",
    "net.ltgt.gradle.incap:incap-processor" to "0.3",
    "org.hamcrest:hamcrest" to "2.2",
    "org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin" to "1.4.32",
    "org.junit:junit-bom" to "5.7.2",
    "org.mockito:mockito-junit-jupiter" to "3.10.0"
)

public fun String.version(): String = versions[this] ?: error("""No version found for $this""")

public fun String.withVersion(): String = "$this:${this.version()}"

public const val GRADLE_VERSION: String = "7.0.2"
