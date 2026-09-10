# Mostly Harmless

An offline-first Android smoking log with a cheerfully cosmic point of view. It records whole and half cigarettes, celebrates resisted urges, and turns the resulting ship's log into useful seven- and thirty-day summaries.

## Features

- One-tap whole- and half-cigarette logging with undo
- Separate resisted-urge entries with an optional note and urge strength
- Editable, filterable history
- Today, seven-day, thirty-day, and custom-range totals
- Dashboard switcher for cigarette equivalents or individual smoking events
- Daily target that remains deliberately non-judgmental
- CSV export through Android's document picker
- Local Room database and DataStore preferences
- Dark, accessible Material 3 interface

The application requests no internet, location, analytics, or account permissions.

## Build

Install Android SDK 35, then run:

```shell
gradle test
gradle assembleDebug
```

Open the project in a current Android Studio release to run it on an API 26+ device or emulator.
The Gradle wrapper is intentionally not checked in because this repository's pull-request
transport does not support binary files; Android Studio can use its bundled Gradle installation.
