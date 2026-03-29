# iTunes Search

An Android app for searching and playing song previews via the Apple iTunes API, built with Jetpack Compose.

https://github.com/user-attachments/assets/787be746-e31d-450f-8f96-a96d72fd0b6c



## Features

**Core:**
- Song search with debounced input (300ms)
- 30-second audio preview playback with seek, next/prev, repeat
- Album screen with track listing (via iTunes Lookup API)
- Offline-first: Room as single source of truth, cached search results and audio previews
- Recently played songs on home screen

**Extras:**
- Mini player with progress bar (persists across screens)
- Connectivity-aware: app-wide offline banner with auto-dismiss on reconnect
- Pull to refresh on search results
- Swipe-to-dismiss on recently played items
- Portuguese (pt) localization
- Audio preview caching for offline playback (Media3 SimpleCache)
- Accessibility: content descriptions on all interactive elements
- Maestro E2E integration test

## Architecture

**MVVM + Unidirectional Data Flow**, following [Android architecture guidelines](https://developer.android.com/topic/architecture):

```
UI (Compose) ──event──> ViewModel ──call──> Repository ──fetch──> Retrofit
UI (Compose) <──state── ViewModel <──flow── Repository <──write── Room <── API
```

- **State flows down** via `StateFlow` collected with `collectAsStateWithLifecycle()`
- **Events flow up** via sealed interfaces (`HomeEvent`, `PlayerEvent`, etc.)
- **Room is the single source of truth** — UI never reads from API directly
- **No exceptions reach the UI** — all errors are caught in `safeApiCall()` and mapped to `UiError`

### Key Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Architecture | MVVM + UDF | Required by spec. Clear data flow, testable. |
| DI | Manual (`AppContainer`) | 4 screens, ~10 objects. Compile-time safe, zero deps. |
| Database | Room | Android-idiomatic, Flow integration, compile-time SQL checks. |
| Network | Retrofit + kotlinx.serialization | Type-safe, Kotlin-native, no reflection. |
| State | `StateFlow` + sealed events | Standard Android, lifecycle-aware, testable with Turbine. |
| Error handling | `Result<T>` sealed interface | Explicit, exhaustive `when`, no leaking exceptions. |
| Images | Coil 3 | Compose-native, built-in memory + disk caching. |
| Media | Media3 ExoPlayer | Modern, handles streaming/seek/caching. |
| Testing | Fakes over mocks | Readable, no reflection magic. Google-recommended. |
| Navigation | Compose Navigation (type-safe) | Supports shared element transitions, ecosystem standard. |

Full decision log with alternatives and trade-offs: [`docs/DECISIONS.md`](docs/DECISIONS.md)

### Offline-First Strategy

1. User searches → API call → results written to Room
2. UI collects `Flow<List<Song>>` from Room (reactive)
3. API fails → Room serves cached data → UI shows stale results + offline banner
4. Audio previews cached via Media3 `SimpleCache` (50MB LRU) — replays work offline

### Error Handling

```
IOException → Result.Failure.Network → UiError.Network → R.string.error_network
HttpException → Result.Failure.Api → UiError.Server → R.string.error_api
CancellationException → rethrown (preserves structured concurrency)
```

Each layer only knows what it needs. Domain has full details for logging, UI gets a simple enum for display.

## Project Structure

```
app/src/main/java/com/bruno/itunessearch/
├── data/           # Remote (Retrofit), Local (Room), Repository implementations
├── domain/         # Models (Song, Album), Repository interfaces, Result<T>
├── ui/             # Screens (home, player, album, splash), Components, Theme
├── player/         # AudioPlayer wrapper, NowPlayingState
├── di/             # AppContainer, LocalAppContainer (CompositionLocal)
├── App.kt          # Application class
├── MainActivity.kt # Single activity, edge-to-edge, connectivity banner
└── Navigation.kt   # Type-safe routes, NavHost, mini player, bottom sheet
```

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material3 |
| Navigation | Compose Navigation (type-safe routes) |
| Database | Room 2.8 |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Serialization | kotlinx.serialization |
| Images | Coil 3 |
| Media | Media3 ExoPlayer |
| Async | Kotlin Coroutines + Flow |
| Testing | JUnit4 + Turbine + Fakes |
| E2E | Maestro |

**Min SDK:** 26 | **Target SDK:** 36 | **Kotlin:** 2.2.10

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17
- Android SDK 36

### Build & Run
```bash
# Clone
git clone https://github.com/brunovsiqueira/ItunesSearch.git
cd ItunesSearch

# Build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run E2E test (requires Maestro CLI: https://maestro.mobile.dev)
maestro test .maestro/main_flow.yaml
```

## Testing

**Unit tests (26 tests):** ViewModels, Result mapping, error handling — all using hand-written fakes (no mocking frameworks).

**E2E (Maestro):** Full user journey — splash → search → play song → back → recently played.

```bash
./gradlew testDebugUnitTest  # Unit tests
maestro test .maestro/       # E2E tests
```
