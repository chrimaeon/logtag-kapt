// Generated with dependencyUpdates on 1/16/21 11:44 AM
// DO NOT EDIT
@file:Suppress(
    "SpellCheckingInspection",
    "RedundantVisibilityModifier"
)

private val versions: Map<String, String> = mapOf(
    "com.android.tools.build:gradle" to "4.1.1",
    "com.android.tools.lint:lint" to "27.1.1",
    "com.android.tools.lint:lint-tests" to "27.1.1",
    "com.android.tools:testutils" to "27.1.1",
    "com.cmgapps.gradle:gradle-dependencies-versions-plugin" to "1.6.0",
    "com.github.tschuchortdev:kotlin-compile-testing" to "1.3.4",
    "com.google.auto.service:auto-service" to "1.0-rc7",
    "com.jfrog.bintray:com.jfrog.bintray.gradle.plugin" to "1.8.5",
    "com.pinterest:ktlint" to "0.40.0",
    "com.squareup:javapoet" to "1.13.0",
    "com.squareup:kotlinpoet" to "1.7.2",
    "junit:junit" to "4.13.1",
    "net.ltgt.gradle.incap:incap-processor" to "0.3",
    "org.hamcrest:hamcrest-library" to "2.2",
    "org.junit:junit-bom" to "5.7.0",
    "org.mockito:mockito-junit-jupiter" to "3.7.7"
)

public fun String.version(): String = versions[this] ?: error("""No version found for $this""")

public fun String.withVersion(): String = "$this:${this.version()}"

public const val GRADLE_VERSION: String = "6.8"
