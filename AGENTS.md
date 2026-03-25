# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

**ImpostorGame** is an Android party game (similar to "Among Us/Mafia") for local multiplayer. Players are secretly assigned roles (civilian, impostor, or "Señor Blanco") and must deduce who the impostor is. The project also includes a Google Apps Script backend that automates suggestion handling via Gemini AI, Gmail, and Notion.

All UI text, comments, and commit messages are in **Spanish**.

## Build Commands

### Android App
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Build App Bundle (AAB)
./gradlew bundleRelease

# Run unit tests
./gradlew test

# Run instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests "com.ricardomodino.impostorgame.ExampleUnitTest"
```

### Google Apps Script (`ImpostorGameScript.gs`)
This is manually deployed via [script.google.com](https://script.google.com) (account: modinoricardo@gmail.com):
1. Paste the file contents into a new GAS project
2. Run `setProperties()` to register credentials (accepts Google permissions)
3. Run `setupTrigger()` to create a 5-minute recurring trigger
4. The script monitors Gmail for replies to `[ImpostorGame]`-tagged emails and creates Notion tasks

## Credentials & local.properties

Sensitive credentials live in `local.properties` (not in git). The app reads them at build time via `BuildConfig`:

```
GEMINI_API_KEY=...       # Google Gemini API key (gemini-2.0-flash)
GMAIL_SENDER=...         # Gmail account for sending suggestions
GMAIL_PASSWORD=...       # App-specific password for Gmail SMTP
GMAIL_RECEIVER=...       # Developer email that receives suggestions
```

## Architecture

### Android (MVVM + Bottom Sheet pattern)

- **Activities** (`activities/`) — 8 screens following the game flow:
  `SplashActivity` → `MainActivity` (config) → `CountdownActivity` → `GuessWordActivity` → `PlayGameActivity` → `VoteActivity` → `ImpostorRevealActivity` → `VictoryActivity`

- **Bottom Sheets** (`bottomsheets/`) — All major UI panels are `BottomSheetDialogFragment`s (game mode, categories, players, theme, suggestions, about)

- **ViewModels** (`viewmodel/`) — `PlayerViewModel` and `CategoryViewModel` hold game state as LiveData observed by `MainActivity`

- **Managers** (`managers/`) — Singletons for cross-cutting concerns: `ThemeManager` (3 neon themes), `SelfieManager` (camera), `SoundManager`, `GameDialog`

- **Models** (`modelos/`) — `Jugador` (player + role), `Category` (word list + emoji), `GameOptions` (all game settings as a data class)

### Game State Flow
Settings are persisted via **SharedPreferences**. `GameOptions` is passed between activities as an `Intent` extra. Player roles (NORMAL / IMPOSTOR / SENOR_BLANCO) are assigned in `CountdownActivity` and passed forward.

### Suggestion Pipeline (GAS backend)
`SugerenciasBottomSheet` → validates with Gemini API → sends via Gmail SMTP → developer replies → `ImpostorGameScript.gs` detects reply → creates Notion task with user metadata.

### Theme System
`ThemeManager` applies one of 3 themes (CLASICO/cyan, CARMESI/red, JMC/green) by swapping drawable resources. Theme is stored in SharedPreferences and applied at Activity start.

## Key Dependencies
- **Coroutines** — All network calls (Gemini, Gmail SMTP) run on `Dispatchers.IO`
- **AndroidX Camera (camera2)** — Optional in-game selfie feature
- **JavaMail (android-mail)** — Gmail SMTP for suggestion emails
- **FlexboxLayoutManager** — Responsive player/category grids in MainActivity
- **Material3** — UI components and theming base

## Game Modes
- **Classic** (`modoMisterioso = false`): Find the impostor(s)
- **Mysterious** (`modoMisterioso = true`): Adds "Señores Blancos" — wild-card players with secret objectives
- **Crazy mode** (`modoLoco = true`): Words are randomized across categories
- **Hint types**: `PISTA_COMPLETA` (full word shown) or `PISTA_PRIMERA_LETRA` (first letter only for impostors)
