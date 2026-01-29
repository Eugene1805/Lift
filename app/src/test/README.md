# Test Structure Documentation

This document describes the professional test structure for the Lift application following industry standards.

## Test Pyramid Structure

```
        /\
       /  \  E2E Tests (10%)
      /----\
     / UI   \ UI/Integration Tests (20%)
    /--------\
   /   Unit   \ Unit Tests (70%)
  /------------\
```

## Directory Structure

### Unit Tests (`src/test/`)
Fast JVM tests that don't require Android dependencies.

```
app/src/test/java/com/eugene/lift/
├── domain/
│   ├── usecase/
│   │   ├── SaveExerciseUseCaseTest.kt
│   │   ├── GetExercisesUseCaseTest.kt
│   │   └── GetExerciseDetailUseCaseTest.kt
│   └── repository/
│       └── ExerciseRepositoryTest.kt
├── ui/
│   └── viewmodel/
│       └── AddExerciseViewModelTest.kt
└── util/
    ├── ExtensionsTest.kt
    └── MainDispatcherRule.kt
```

### Instrumented Tests (`src/androidTest/`)
Tests that require Android framework (emulator/device).

```
app/src/androidTest/java/com/eugene/lift/
├── data/
│   ├── dao/
│   │   └── ExerciseDaoTest.kt
│   └── repository/
│       └── ExerciseRepositoryImplTest.kt
├── ui/
│   └── feature/
│       └── exercises/
│           └── AddExerciseScreenTest.kt
└── di/
    └── TestAppModule.kt
```

## Test Types

### 1. Unit Tests (70%)
**Location**: `src/test/` (JVM tests)
**Purpose**: Test business logic in isolation
**Tools**: JUnit, MockK, Coroutines Test

**Examples**:
- ViewModel logic tests
- UseCase business rules
- Utility functions
- Data transformations

**Characteristics**:
- ✅ Fast (milliseconds)
- ✅ No Android dependencies
- ✅ Easy to debug
- ✅ Run on CI/CD easily

### 2. Integration Tests (20%)
**Location**: `src/androidTest/` (Instrumented)
**Purpose**: Test component interactions
**Tools**: AndroidX Test, Room in-memory DB

**Examples**:
- Database operations (DAOs)
- Repository implementations
- Data layer integration

**Characteristics**:
- ⏱️ Slower (seconds)
- 📱 Requires emulator/device
- 🔗 Tests real Android components

### 3. UI Tests (10%)
**Location**: `src/androidTest/` (Instrumented)
**Purpose**: Test user interactions and flows
**Tools**: Compose Test, Espresso

**Examples**:
- Screen navigation
- User input validation
- Complete user flows

**Characteristics**:
- ⏱️ Slowest (minutes)
- 📱 Requires emulator/device
- 🎯 Tests end-to-end scenarios

## Test Naming Convention

### Test Class Names
- Append `Test` to the class being tested
- Example: `AddExerciseViewModel` → `AddExerciseViewModelTest`

### Test Method Names
Use backticks for descriptive names:
```kotlin
@Test
fun `saveExercise calls use case with correct data when name is valid`() = runTest {
    // Test implementation
}
```

**Format**: `methodName does something when condition`

## Test Structure (AAA Pattern)

All tests follow the **Arrange-Act-Assert** pattern:

```kotlin
@Test
fun `example test`() = runTest {
    // GIVEN (Arrange)
    val viewModel = AddExerciseViewModel(useCase, savedStateHandle)
    
    // WHEN (Act)
    viewModel.saveExercise()
    
    // THEN (Assert)
    verify { useCase.invoke(any()) }
}
```

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Test Class
```bash
./gradlew test --tests AddExerciseViewModelTest
```

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Best Practices

### ✅ DO
- Use descriptive test names with backticks
- Mock external dependencies with MockK
- Use `runTest` for coroutines
- Test edge cases and error conditions
- Keep tests independent
- Use in-memory database for Room tests
- Follow AAA pattern (Arrange-Act-Assert)

### ❌ DON'T
- Test Android framework behavior
- Make tests depend on each other
- Use real network calls
- Use real database in unit tests
- Write tests that are flaky
- Test implementation details

## Key Testing Libraries

### Core Testing
```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("app.cash.turbine:turbine:1.0.0")
```

### Mocking
```kotlin
testImplementation("io.mockk:mockk:1.13.8")
```

### Android Testing
```kotlin
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test:core-ktx:1.5.0")
androidTestImplementation("androidx.room:room-testing:2.6.1")
```

## Test Coverage Goals

- **Unit Tests**: Aim for 70-80% coverage
- **Critical Business Logic**: 90%+ coverage
- **ViewModels**: 80%+ coverage
- **Use Cases**: 90%+ coverage

## Continuous Integration

Tests are automatically run on:
- Every commit
- Pull requests
- Before releases

### GitHub Actions Example
```yaml
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Instrumented Tests
  run: ./gradlew connectedAndroidTest
```

## Troubleshooting

### Tests are slow
- Check if using instrumented tests instead of unit tests
- Use `runTest` for coroutine tests
- Mock external dependencies

### Flaky tests
- Remove Thread.sleep, use proper coroutine testing
- Don't depend on timing
- Ensure proper test isolation

### Database issues
- Use in-memory database for tests
- Clear database in `@After` methods
- Don't reuse database instances

## Examples

See the following test files for reference:
- `AddExerciseViewModelTest.kt` - ViewModel testing
- `SaveExerciseUseCaseTest.kt` - UseCase testing
- `ExerciseDaoTest.kt` - Database testing (instrumented)

## Resources

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [JUnit 4 Documentation](https://junit.org/junit4/)
- [MockK Documentation](https://mockk.io/)
- [Turbine (Flow Testing)](https://github.com/cashapp/turbine)
