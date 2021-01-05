// Generated with dependencyUpdates on 1/5/21 6:38 PM
// DO NOT EDIT
@file:Suppress(
    "SpellCheckingInspection",
    "RedundantVisibilityModifier"
)

private val versions: Map<String, String> = mapOf(
    "com.cmgapps.gradle:gradle-dependencies-versions-plugin" to "1.6.0",
    "com.github.tschuchortdev:kotlin-compile-testing" to "1.3.4",
    "com.google.auto.service:auto-service" to "1.0-rc7",
    "com.pinterest:ktlint" to "0.40.0",
    "com.squareup:kotlinpoet" to "1.7.2",
    "junit:junit" to "4.13.1",
    "net.ltgt.gradle.incap:incap-processor" to "0.3",
    "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin" to "1.4.21-2",
    "org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin" to "1.4.21-2"
)

public fun String.version(): String = versions[this] ?: error("""No version found for $this""")

public fun String.withVersion(): String = "$this:${this.version()}"

public const val GRADLE_VERSION: String = "6.7.1"
