/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")
@file:OptIn(ExperimentalWasmDsl::class)

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinUsages
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8EnvSpec
import org.jetbrains.kotlin.gradle.targets.wasm.d8.D8Plugin

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.buildconfig)
    `maven-publish`
    signing
    alias(libs.plugins.gradle.java.test.fixtures)
    alias(libs.plugins.gradle.idea)
    id("ktlint")
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.kover)
}

project.plugins.apply(D8Plugin::class.java)

val versionName: String = project.findProperty("versionName") as String
project.version = "${getKotlinPluginVersion()}-$versionName"

val testDataDir = layout.projectDirectory.dir("testData")
val testGenDirectory = layout.buildDirectory.dir("test-gen")

sourceSets {
    test {
        java.setSrcDirs(listOf(testGenDirectory))
        resources.setSrcDirs(listOf(testDataDir))
    }
}

idea {
    // This is needed until IDEA fixes https://youtrack.jetbrains.com/issue/IDEA-339729.
    module.generatedSourceDirs.add(testGenDirectory.get().asFile)
}

val testArtifacts: Configuration = configurations.create("testArtifact")

val annotationsRuntimeClasspath =
    configurations.dependencyScope("annotationsRuntimeClasspath") {
        isTransitive = false
    }
val annotationsJvmRuntimeClasspath =
    configurations.resolvable("annotationsJvmRuntimeClasspath") {
        extendsFrom(annotationsRuntimeClasspath)
    }

val annotationsJsRuntimeClasspath =
    configurations.resolvable("annotationsJsRuntimeClasspath") {
        extendsFrom(annotationsRuntimeClasspath)
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(KotlinUsages.KOTLIN_RUNTIME))
            attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
        }
    }

java {
    withSourcesJar()
    withJavadocJar()
}

buildConfig {
    packageName.set("com.cmgapps.logtag")
    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")
}

publishing {
    publications {
        register<MavenPublication>("compiler-plugin") {
            from(components["java"])

            pom {
                val artifactId: String = project.name
                val name: String = project.findProperty("name") as String
                val description: String = project.findProperty("description") as String
                val scmUrl: String = project.findProperty("scmUrl") as String
                val connectionUrl: String = project.findProperty("connectionUrl") as String
                val developerConnectionUrl: String = project.findProperty("developerConnectionUrl") as String
                val projectUrl: String = project.findProperty("projectUrl") as String

                groupId = project.group.toString()
                setArtifactId(artifactId)
                version = project.version.toString()

                this.name.set(name)
                this.description.set(description)
                this.url.set(projectUrl)
                developers {
                    developer {
                        this.id.set("chrimaeon")
                        this.name.set("Christian Grach")
                        this.email.set("christian.grach@cmgapps.com")
                    }
                }

                scm {
                    this.url.set(scmUrl)
                    this.connection.set(connectionUrl)
                    this.developerConnection.set(developerConnectionUrl)
                }

                issueManagement {
                    this.url.set("$projectUrl/issues")
                    this.system.set("github")
                }

                licenses {
                    license {
                        this.name.set("Apache-2.0")
                        this.url.set("https://spdx.org/licenses/Apache-2.0.html")
                    }
                }
            }
        }

        signing {
            isRequired = !versionName.endsWith("SNAPSHOT")
            sign(publications)
        }

        repositories {
            // TODO setup central sonatype
            mavenLocal()
        }
    }
}

tasks.test {
    dependsOn(testArtifacts)
    dependsOn(annotationsJvmRuntimeClasspath)
    dependsOn(annotationsJsRuntimeClasspath)

    useJUnitPlatform()
    workingDir = rootDir

    systemProperty("annotationsRuntime.jvm.classpath", annotationsJvmRuntimeClasspath.get().asPath)
    systemProperty("annotationsRuntime.js.classpath", annotationsJsRuntimeClasspath.get().asPath)

    // Properties required to run the internal test framework.
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib", "kotlin-stdlib")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-jdk8", "kotlin-stdlib-jdk8")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-reflect", "kotlin-reflect")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test", "kotlin-test")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-script-runtime", "kotlin-script-runtime")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-annotations-jvm", "kotlin-annotations-jvm")

    systemProperty("idea.ignore.disabled.plugins", "true")
    systemProperty("idea.home.path", rootDir)

    // see https://github.com/Kotlin/compiler-plugin-template/blob/052542900bb147ab4225a87c9efd05a5ef53695b/compiler-plugin/build.gradle.kts
    // Properties required to run JS tests from the internal test framework.
    val d8EnvSpec = project.the<D8EnvSpec>()
    with(d8EnvSpec) { dependsOn(project.d8SetupTaskProvider) }

    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-stdlib-js", "kotlin-stdlib-js")
    setLibraryProperty("org.jetbrains.kotlin.test.kotlin-test-js", "kotlin-test-js")

    systemProperty("javascript.engine.path.V8", d8EnvSpec.executable.get())
    systemProperty("javascript.engine.path.repl", "${layout.projectDirectory.file("repl.js").asFile}")
    systemProperty("kotlin.js.test.root.out.dir", "${layout.buildDirectory.dir("js-test-output").get().asFile}")

    testLogging {
        events("PASSED", "SKIPPED", "FAILED")
    }

    maxHeapSize = "2g"
    jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
}

kover {
    useJacoco()

    currentProject {
        sources {
            excludedSourceSets.addAll("testFixtures")
        }
    }

    reports {
        verify {
            rule("Minimal line coverage") {
                bound {
                    minValue = 80
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }

        total {
            log {
                onCheck = true
                header = "Total Test Line Coverage"
                groupBy = GroupingEntityType.APPLICATION
                aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                coverageUnits = CoverageUnit.LINE
                format = "<value>% total line coverage"
            }
        }
    }
}

val generateTests =
    tasks.register("generateTests", JavaExec::class.java) {
        group = "verification"
        description = "Generate tests for the compiler plugin."
        inputs
            .dir(testDataDir)
            .withPropertyName("testData")
            .withPathSensitivity(PathSensitivity.RELATIVE)
        outputs
            .dir(testGenDirectory)
            .withPropertyName("generatedTests")

        classpath = sourceSets.testFixtures.get().runtimeClasspath
        mainClass.set("com.cmgapps.logtag.compiler.GenerateTestsKt")
        workingDir = rootDir
        args(
            listOf(
                testGenDirectory.get().asFile.absolutePath,
                testDataDir.asFile.absolutePath,
            ),
        )
    }

tasks.compileTestKotlin {
    dependsOn(generateTests)
}

dependencies {
    compileOnly(libs.kotlin.compiler)

    // kotlin-compiler-internal-test-framework is compiled against JUnit >= 5.12
    // (JUnit5Assertions calls AssertionsKt.fail_nonNullableLambda), while
    // kotlin-test-junit5 only brings in 5.10.x transitively.
    testFixturesApi(platform(libs.junit.bom))
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.kotlin.test.junit5)
    testFixturesApi(libs.kotlin.test.framework)
    testFixturesApi(libs.kotlin.compiler)
    testFixturesRuntimeOnly(libs.junit)

    annotationsRuntimeClasspath(projects.annotation)

    // Dependencies required to run the internal test framework.
    testArtifacts(libs.kotlin.stdlib)
    testArtifacts(libs.kotlin.stdlib.jdk8)
    testArtifacts(libs.kotlin.reflect)
    testArtifacts(libs.kotlin.test)
    testArtifacts(libs.kotlin.script.runtime)
    testArtifacts(libs.kotlin.annotations.jvm)

    testArtifacts(libs.kotlin.stdlib.js)
    testArtifacts(libs.kotlin.test.js)
}

fun Test.setLibraryProperty(
    propName: String,
    jarName: String,
) {
    val path =
        testArtifacts.files
            .find { """$jarName-\d.*""".toRegex().matches(it.name) }
            ?.absolutePath
            ?: return
    systemProperty(propName, path)
}
