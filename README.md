# Charge-me Not

A local-first personal finance app by **ART-ie LLC** that helps you tend your financial garden — track upcoming bills, visualize spending by category, and decide which subscriptions to keep or pull.

## Stack

- **Language:** 100% Kotlin
- **UI:** Jetpack Compose with Material 3 (no XML layouts)
- **Architecture:** Clean Architecture with MVVM and StateFlow
- **Persistence:** Room Database (local-only, no cloud or external APIs)

## Features

- **Dashboard** with meadow-inspired Material 3 theme
- **Total Upcoming** summary of unpaid bills
- **Your Financial Bloom** — a Canvas-drawn flower where each petal represents a spending category
- **Subscriptions: Weeds or Flowers?** — manage recurring bills with Keep (leaf) and Pull (weed) actions
- **Pruning Simulator** — ephemeral financial sandbox to test pruning bills without writing to Room
- **Nag Mode** — local WorkManager reminders for due/overdue bills
- **Bill Scanner** — CameraX OCR with predictive bloom impact

## Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 36

## Getting Started

1. Open the project in Android Studio.
2. Sync Gradle.
3. Run on an emulator or device (API 26+).

```bash
./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/artie/chargemenot/
├── data/
│   ├── local/          # Room entities, DAO, database
│   └── repository/     # Data repository
├── domain/
│   └── model/          # Domain models
├── ui/
│   ├── dashboard/      # ViewModel & UI state
│   ├── screens/        # Compose screens (Dashboard, Scanner, Pruning Simulator)
│   ├── viewmodels/     # Scanner, Settings, Pruning ViewModels
│   └── theme/          # Meadow color palette & typography
├── MainActivity.kt
└── ChargeMeNotApplication.kt
```

## Privacy

All data stays on-device. No network permissions, no cloud sync, no external APIs.

## License

Copyright © ART-ie LLC. All rights reserved.
