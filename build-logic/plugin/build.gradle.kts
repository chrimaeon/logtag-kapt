/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 */

plugins {
    `kotlin-dsl`
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
