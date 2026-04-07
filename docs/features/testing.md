# Testing & Verification

## Overview

This project includes unit-test support and CI-oriented security checks.
The test setup was updated to be more stable in environments where Firebase configuration files are not present.

## Local Unit Tests

Use the Gradle task below to run unit tests:

```bash
./gradlew testDebugUnitTest --no-daemon
```

### What the tests cover

Current tests cover:

- user model behavior,
- sign-in ViewModel validation flow,
- sign-up ViewModel validation and repository behavior.

## Coroutine Test Support

The test suite uses a dedicated `MainDispatcherRule` so ViewModel coroutines run deterministically during tests.

## Security / Dependency Checks

The project also includes dependency security scanning through the OWASP Dependency-Check Gradle plugin.
This is intended to catch vulnerable dependencies and keep the build secure over time.

## Related Files

- `app/src/test/java/com/example/mobiledev/test/MainDispatcherRule.kt`
- `app/src/test/java/com/example/mobiledev/data/model/UserTest.kt`
- `app/src/test/java/com/example/mobiledev/feature/signin/presentation/SignInViewModelTest.kt`
- `app/src/test/java/com/example/mobiledev/feature/signup/presentation/SignUpViewModelTest.kt`
- `app/build.gradle.kts`
- `app/dependency-check-suppressions.xml`

