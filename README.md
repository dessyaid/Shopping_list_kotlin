# Shopping List App

A simple Android shopping-list app built with Jetpack Compose and Room.

## CI/CD Configuration

This project uses **GitHub Actions** for continuous integration.

### Setup
The workflow is defined in `.github/workflows/test.yml`. It triggers on every `push` or `pull_request` to the `main` branch.

### Workflow Steps:
1. **Checkout**: Clones the repository.
2. **Setup JDK 11**: Installs the Temurin distribution of Java 11.
3. **Execute Permissions**: Grants execution rights to `gradlew`.
4. **Unit Tests**: Runs `./gradlew testDebugUnitTest` to verify core logic and Room DAO operations (using Robolectric).

### Instrumented Tests
Instrumented tests (UI tests) currently require a running device or emulator. In this basic CI setup, they are skipped on the standard runner. To run them locally, use:
`./gradlew connectedDebugAndroidTest`

### Logs
Results of the test execution can be found in the **Actions** tab of the GitHub repository.
