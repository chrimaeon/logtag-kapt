# Log TAG Annotation Processor [![CircleCI](https://circleci.com/gh/chrimaeon/logtag-kapt.svg?style=svg)](https://circleci.com/gh/chrimaeon/logtag-kapt)


[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![MavenCentral](https://img.shields.io/maven-central/v/com.cmgapps.logtag/log-tag?style=for-the-badge)](https://repo1.maven.org/maven2/com/cmgapps/logtag/)

This is an annotation processor that will generate an appropriate log tag for Android Log messages

## Usage

### Setup

#### KAPT

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps:log-tag:0.2.0")
    kapt("com.cmgapps:log-tag-processor:0.2.0")
}
```

also get sure to apply the Annotation Processor Plugin

```kotlin
plugins {
    kotlin("kapt")
}
```

#### KSP

The library also supports KSP ([Kotlin Symbol Processing API]), which is currently in a beta state, when you projects kotin version is `1.5.10` or higher

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps:log-tag:0.2.0")
    ksp("com.cmgapps:log-tag-processor:0.2.0")
}
```

also get sure to apply the KSP Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.5.10-1.0.0-beta01"
}
```

### Code

In your source file add the `com.cmgapps.LogTag` annotation to the class file you want to have a log tag generated:

```kotlin

@com.cmgapps.LogTag
class SuperImportantClass
```

For _Kotlin_ classes this will generate an extension property to you class called `LOG_TAG`
you can then use as the tag for your android log messages.

For _Java_ it will generate a class called &lt;Classname&gt;LogTag which has a constant field called `LOG_TAG` you can
then import to tag your android log messages

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
