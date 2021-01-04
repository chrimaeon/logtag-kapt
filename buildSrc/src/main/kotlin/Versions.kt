// Generated with dependencyUpdates on 1/4/21 2:09 PM
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
    "com.cmgapps.gradle:gradle-dependencies-versions-plugin" to "1.6.0",
    "com.pinterest:ktlint" to "0.40.0",
    "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin" to "1.4.21-2"
)


public fun String.version(): String = versions[this] ?: error("""No version found for $this""")

public fun String.withVersion(): String = "$this:${this.version()}"

public const val GRADLE_VERSION: String = "6.7.1"
