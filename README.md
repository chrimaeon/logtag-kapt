# Log TAG Annotation Processor [![Build & Test](https://github.com/chrimaeon/logtag-kapt/actions/workflows/main.yml/badge.svg)](https://github.com/chrimaeon/logtag-kapt/actions/workflows/main.yml) [![codecov](https://codecov.io/gh/chrimaeon/logtag-kapt/branch/main/graph/badge.svg?token=QH5OYAQUX3)](https://codecov.io/gh/chrimaeon/logtag-kapt)

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![MavenCentral](https://img.shields.io/maven-central/v/com.cmgapps.logtag/log-tag?style=for-the-badge)](https://repo1.maven.org/maven2/com/cmgapps/logtag/)

This is an annotation processor that will generate an appropriate log tag for Android Log messages

## Usage

### Setup

<details open>

<summary>using KSP</summary>

The library supports KSP ([Kotlin Symbol Processing API])

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps:log-tag:1.1.0")
    ksp("com.cmgapps:log-tag-processor:1.1.0")
}
```

also get sure to apply the KSP Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.1.0"
}
```

</details>

<details>

<summary>using KAPT</summary>

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps:log-tag:1.1.0")
    kapt("com.cmgapps:log-tag-processor:1.1.0")
}
```

also get sure to apply the Annotation Processor Plugin

```kotlin
plugins {
    kotlin("kapt")
}
```

</details>

### Code

In your source file add the `com.cmgapps.LogTag` annotation to the class file you want to have a log tag generated:

```kotlin

@com.cmgapps.LogTag
class SuperImportantClass
```

* For **Kotlin** classes this will generate an extension property to you class called `LOG_TAG`
  you can then use as the tag for your android log messages.

* For **Java** it will generate a class called `<Classname>LogTag` which has a constant field called `LOG_TAG` you can
  then import to tag your android log messages

* For **Jetpack Compose** you can annotate the `@Composable` function for the processor to generate a class called
  `Composable<Composable function name>` with a companion object property `LOG_TAG`

## License

```text
Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[Kotlin Symbol Processing API]: https://github.com/google/ksp
