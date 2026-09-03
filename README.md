# Log TAG Generator [![Build & Test](https://github.com/chrimaeon/logtag-kapt/actions/workflows/main.yml/badge.svg)](https://github.com/chrimaeon/logtag-kapt/actions/workflows/main.yml) [![codecov](https://codecov.io/gh/chrimaeon/logtag-kapt/branch/main/graph/badge.svg?token=QH5OYAQUX3)](https://codecov.io/gh/chrimaeon/logtag-kapt)

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![MavenCentral](https://img.shields.io/maven-central/v/com.cmgapps.logtag/log-tag?style=for-the-badge)](https://repo1.maven.org/maven2/com/cmgapps/logtag/)

This is an annotation processor that will generate an appropriate log tag for Android Log messages

You can use the library with either the [Kotlin Compiler Plugin](#Using-Kotlin-Compiler-Plugin) or
an [Annotation Processor](#Using-Annotation-Processors)

## Using Kotlin Compiler Plugin

> [!NOTE]
> Currently, this feature is experimental.
>
> The K2 Kotlin IntelliJ plugin supports running third party FIR plugins in the IDE, but this feature is hidden behind a flag.
>
> To enable it, do the following:
>
> 1. Enable K2 Mode for the Kotlin IntelliJ plugin.
> 2. Open the Registry
> 3. Set the kotlin.k2.only.bundled.compiler.plugins.enabled entry to false.
>
> That support is unstable and subject to change.

Apply the compiler plugin with Gradle

```kotlin
plugins {
    id("com.cmgapps.logtag") version "2.0.0-alpha.1"
}
```

Now you can use the `@LogTag` annotation in your source files

```kotlin
@com.cmgapps.LogTag
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(LOG_TAG, "onCreate")
    }
}
```

The compiler will generate a private property `LOG_TAG` with the class' name that you can use as the tag for your
log messages.

## Using Annotation Processors

<details open>

<summary>using KSP</summary>

The library supports KSP ([Kotlin Symbol Processing API])

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps.logtag:log-tag:2.0.0-alpha.1")
    ksp("com.cmgapps.logtag:processor:2.0.0-alpha.1")
}
```

also get sure to apply the KSP Gradle Plugin

```kotlin
plugins {
    id("com.google.devtools.ksp") version "<ksp-version>"
}
```

</details>

<details>

<summary>using KAPT</summary>

Add the processor and annotation libraries to the projects dependencies

```kotlin
dependencies {
    implementation("com.cmgapps.logtag:log-tag:2.0.0-alpha.1")
    kapt("com.cmgapps.logtag:processor:2.0.0-alpha.1")
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
Copyright (c) 2021-2026. Christian Grach <christian.grach@cmgapps.com>

SPDX-License-Identifier: Apache-2.0
```

[Kotlin Symbol Processing API]: https://github.com/google/ksp
