# Log TAG Annotation Processor

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![MavenCentral](https://www.cmgapps.com/badge/chrimaeon/maven/com.cmgapps:log-tag/badge.svg)](https://bintray.com/chrimaeon/maven/com.cmgapps:log-tag)

This is an annotation processor that will generate an appropriate log tag for Android Log messages

## Usage

### Setup

Add the KAPT and annotation to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps:log-tag:0.1.0")
    kapt("com.cmgapps:log-tag-processor:0.1.0")
}
```

also get sure to apply the Annotation Processor Plugin

```kotlin
plugins {
    kotlin("kapt")
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
