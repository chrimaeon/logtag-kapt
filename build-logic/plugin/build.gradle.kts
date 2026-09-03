/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.gradle.plugin)
}

group = "com.cmgapps.gradle.buildlogic.plugin"

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        register("ktlintPlugin") {
            id = "ktlint"
            implementationClass = "com.cmgapps.gradle.KtlintPlugin"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
