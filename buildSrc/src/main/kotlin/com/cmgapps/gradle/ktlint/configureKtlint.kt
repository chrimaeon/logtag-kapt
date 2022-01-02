/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.gradle.ktlint

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.invoke
import withVersion

fun Project.configureKtlint() {

    val ktlintConfiguration = configurations.create("ktlint")

    tasks {

        val inputFiles = fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))
        val outputDir = "${buildDir}/reports"

        register("ktlintFormat", JavaExec::class.java) {
            inputs.files(inputFiles)
            outputs.dir(outputDir)

            group = "Formatting"
            description = "Fix Kotlin code style deviations."
            mainClass.set("com.pinterest.ktlint.Main")
            classpath = ktlintConfiguration
            args = listOf("-F", "src/**/*.kt")
        }

        val ktlintTask = register("ktlint", JavaExec::class.java) {
            inputs.files(inputFiles)
            outputs.dir(outputDir)

            group = "Verification"
            description = "Check Kotlin code style."
            mainClass.set("com.pinterest.ktlint.Main")
            classpath = ktlintConfiguration
            args = listOf(
                "src/**/*.kt",
                "--reporter=plain",
                "--reporter=html,output=${outputDir}/ktlint.html"
            )
        }

        named("check") {
            dependsOn(ktlintTask)
        }

    }

    dependencies.add(ktlintConfiguration.name, "com.pinterest:ktlint".withVersion())
}
