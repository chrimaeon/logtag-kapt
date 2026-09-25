# Changelog

## [Unreleased]

### Added

- Integrated the [https://github.com/chrimaeon/lint-logdebug](https://github.com/chrimaeon/lint-logdebug) project into
  the lint checks

### Changed

- The LogTag Gradle plugin now requires JVM 21+.
- Add Android Linter as a dependency to the LogTag Gradle plugin
- The KSP processor now respects the `logtag.androidMinSdkVersion` option; Android API 26 removes the 23-character limit
  for log tags
- `com.cmgapps.logtag:log-tag` maven module moved to `com.cmgapps.logtag:android-lint`

### Deprecated

### Removed

### Fixed

### Security

## [2.0.0-alpha.1]

### Added

- Kotlin Compiler Plugin (Experimental)

### Changed

- Annotation library `com.cmgapps.logtag:annotation` is not Kotlin Multiplatform compatible

### Deprecated

- LogTag's KAPT will be removed in the next major release; change to the KSP version

[Unreleased]: https://github.com/chrimaeon/logtag-kapt/compare/v2.0.0-alpha.1...HEAD
[2.0.0-alpha.1]: https://github.com/chrimaeon/logtag-kapt/commits/v2.0.0-alpha.1
