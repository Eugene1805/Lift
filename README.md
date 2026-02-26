# Lift

Lift is a modern Android application designed for fitness enthusiasts to track their workouts, manage routines, and monitor progress over time. It provides a robust, offline-first experience with an intuitive user interface built entirely with Jetpack Compose.

## 📱 Features

*   **Workout Tracking:** Start, record, and finish your workouts seamlessly.
*   **Exercise Management:** Browse through exercises, and customize them according to your needs.
*   **Routines & Templates:** Build reusable workout templates, duplicate them, and organize them into custom folders.
*   **Workout History:** View detailed history of past sessions, including exercises, sets, reps, and weights.
*   **Rest Timer:** Built-in rest timer with background support to manage your intervals between sets effectively.
*   **Statistics & Profile:** Track your lifting statistics and customize your user profile.
*   **Customizable Settings:** Support for Dark/Light themes, multiple languages, and interchangeable unit systems (Metric / Imperial).
*   **Offline-First:** Fully functional without an internet connection, storing all data locally.

## 🛠 Tech Stack

This project leverages modern Android development tools and practices:

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **Architecture:** Clean Architecture with MVVM (Model-View-ViewModel) pattern
*   **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
*   **Database:** [Room](https://developer.android.com/training/data-storage/room)
*   **Preferences:** Jetpack DataStore
*   **Asynchronous Programming:** Kotlin Coroutines and Flows
*   **Navigation:** Navigation Compose
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/compose/)
*   **Background Tasks:** WorkManager
*   **Serialization:** Kotlinx Serialization

## 🧪 Testing

The app is built with a strong emphasis on testing, particularly across the domain and data layers to ensure a high level of code reliability and path coverage. Testing libraries include:
*   [JUnit4](https://junit.org/junit4/)
*   [MockK](https://mockk.io/) for mocking dependencies.
*   [Turbine](https://github.com/cashapp/turbine) for testing Kotlin Flows.
*   Coroutines Test for managing background dispatchers.

## 🏗 Architecture

The app is structured into distinct layers following Clean Architecture principles to promote separation of concerns, testability, and scalability:

*   **Domain:** The core layer containing business logic, Use Cases, domain Models, and Repository interfaces. It has no dependencies on the Android framework.
*   **Data:** Implements the repository interfaces defined in the domain layer. It handles data retrieval and storage using Room and DataStore.
*   **UI (Presentation):** Contains the Jetpack Compose screens, ViewModels, and UI state management.

## 🚀 Getting Started

### Prerequisites

*   [Android Studio](https://developer.android.com/studio) (latest recommended)
*   JDK 17 or higher
*   Android SDK 35

### Building and Running

1.  Clone this repository to your local machine:
    ```bash
    git clone <repository-url>
    ```
2.  Open the project in **Android Studio**.
3.  Allow Gradle to sync project dependencies.
4.  Select a physical device or an emulator running API level 26 (Android 8.0) or higher.
5.  Click the **Run** button to install and launch the app.
