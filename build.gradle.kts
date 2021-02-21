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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

buildscript {
    repositories {
        google()
        jcenter()
        helixTeamHubRepo(project)
        // arrow-kt snapshots
        bintraySnapshot()
    }

    dependencies {
        // STOPSHIP
        // classpath("com.android.tools.build:gradle".withVersion())
        classpath(kotlin("gradle-plugin", KOTLIN_VERSION))
        classpath("com.cmgapps.gradle:gradle-dependencies-versions-plugin".withVersion())
        classpath("io.arrow-kt:gradle-plugin".withVersion())
    }
}

apply(plugin = "com.cmgapps.versions")

subprojects {

    val group: String by project
    val versionName: String by project

    this.group = group
    this.version = versionName

    repositories {
        google()
        jcenter()
        mavenCentral()
        // arrow-kt snapshots
        bintraySnapshot()
    }

    pluginManager.withPlugin("maven-publish") {
        val publishExtension = extensions.getByType<PublishingExtension>()
        publishExtension.repositories {
            sonatype(project)
        }

        publishExtension.publications.whenObjectAdded {
            check(this is MavenPublication) {
                "unexpected publication $this"
            }
            logtagPom(project)
        }
    }
}

gradle.projectsEvaluated {
    subprojects {
        tasks {
            withType<JavaCompile> {
                options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xmaxerrs", "500"))
            }

            withType<KotlinCompile> {
                kotlinOptions {
                    jvmTarget = JavaVersion.VERSION_1_8.toString()
                }
            }
        }

        if (plugins.hasPlugin(JavaLibraryPlugin::class)) {
            tasks.named<Jar>("jar") {
                manifest {
                    val pomName: String? by project
                    attributes(
                        "Implementation-Title" to pomName,
                        "Implementation-Version" to project.version,
                        "Built-By" to System.getProperty("user.name"),
                        "Built-Date" to Date(),
                        "Built-JDK" to System.getProperty("java.version"),
                        "Built-Gradle" to gradle.gradleVersion,
                        "Built-Kotlin" to KOTLIN_VERSION
                    )
                }
            }
        }
    }
}

tasks {
    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    named<Wrapper>("wrapper") {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = GRADLE_VERSION
    }
}
