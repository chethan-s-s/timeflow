cd /Users/chethans/AndroidStudioProjects/CountdownTImer
git remote add origin https://github.com/<your-username>/timeflow.git
git push -u origin main# Time Flow

Time Flow is an Android countdown app with widgets, smart reminders, yearly repeats, backup/import, and customizable themes.

## Tech Stack
- Kotlin
- Jetpack Compose
- Room
- Android App Widgets

## Build
```zsh
./gradlew assembleDebug
```

## Release
Release signing is configured via `keystore.properties`, which is intentionally ignored by git.

