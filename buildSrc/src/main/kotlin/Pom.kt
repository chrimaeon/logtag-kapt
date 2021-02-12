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

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.provideDelegate

fun MavenPublication.logtagPom(project: Project) = pom {

    val artifactId: String by project
    val name: String by project
    val description: String by project
    val scmUrl: String by project
    val connectionUrl: String by project
    val developerConnectionUrl: String by project
    val projectUrl: String by project

    this@logtagPom.groupId = project.group.toString()
    this@logtagPom.artifactId = artifactId
    this@logtagPom.version = project.version.toString()

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
        this.url.set("${projectUrl}/issues")
        this.system.set("github")
    }

    licenses {
        license {
            this.name.set("Apache License, Version 2.0")
            this.url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
}
