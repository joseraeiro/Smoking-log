# Mostly Harmless

An offline-first Android smoking log with a cheerfully cosmic point of view. It records whole and half cigarettes, celebrates resisted urges, and turns the resulting ship's log into useful seven- and thirty-day summaries.

## Features

- One-tap whole- and half-cigarette logging with undo
- Separate resisted-urge entries with an optional note and urge strength
- Optional smoking triggers plus coping strategy, duration, and afterward feeling for resisted urges
- Editable, filterable history with recorded times and elapsed time since the previous entry
- Configurable personal-day rollover so after-midnight entries can remain with the previous waking day
- Today, seven-day, thirty-day, and custom-range totals
- Dashboard switcher for cigarette equivalents or individual smoking events
- Guide sections for overview, timing, triggers, coping, and money spent
- Previous-period comparisons, interval statistics, hourly activity, first/last times, and calendar heatmap
- Optional pack price, pack size, and currency settings for estimated spending
- 60 standalone rotating cosmic bulletins for every main screen
- 60 standalone randomized confirmations each for logging and resisting a cigarette (360 messages total)
- Daily target that remains deliberately non-judgmental
- CSV export through Android's document picker
- Transactional CSV import that accepts legacy exports and validates before replacing the local log
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
