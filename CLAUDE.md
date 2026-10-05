# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

AKJ Events is a single-module Android app (`:app`, package `co.edu.uniquindio.akjevents`) for discovering community events, built as a university class project. It is at "Paso 1": all data is local demo data, with no backend, auth, or persistence yet. Code comments, UI strings, and the README are in Spanish; keep new user-facing text and comments in Spanish to match.

Toolchain: JDK 17, Gradle 9.6, AGP 9.4, Kotlin 2.4, compileSdk/targetSdk 37, minSdk 28. Dependency versions live in `gradle/libs.versions.toml`.

## Commands

Use `./gradlew` (Git Bash) or `.\gradlew.bat` (PowerShell) from the repo root.

- Build debug APK: `./gradlew assembleDebug`
- Install on a running emulator/device: `./gradlew installDebug`
- Unit tests (JVM): `./gradlew testDebugUnitTest`
- Single unit test: `./gradlew testDebugUnitTest --tests "co.edu.uniquindio.akjevents.PublicEventVisibilityTest"` (append `.methodName` for one method)
- Instrumented tests (needs device/emulator): `./gradlew connectedDebugAndroidTest`
- Android lint: `./gradlew lint`

## Architecture

Single-Activity Jetpack Compose app (Material 3). `MainActivity` sets `AKJEventsTheme { AppNavigation() }`; everything else is composables.

Source is split by layer under `app/src/main/java/co/edu/uniquindio/akjevents/`:

- `core/` — cross-cutting: `navigation/` (NavHost + routes), `theme/` (AKJ Events prototype colors), shared `component/`s, `util/RequestResult` (Loading/Success/Failure for future async operations).
- `domain/model/` — plain Kotlin models. `CommunityEvent` enforces invariants in `init` via `require` (at least one image, `endsAt` after `startsAt`, positive capacity, `REJECTED` requires `rejectionReason`). Constructing an invalid event throws.
- `domain/repository/EventRepository` + `data/repository/InMemoryEventRepository` — the single source of event data and of the session user's attendance/interest, exposed as `StateFlow`s. `InMemoryEventRepository.shared` is the app-wide instance (no DI yet); ViewModels take the repository as a constructor parameter defaulting to it, so tests pass a fresh `InMemoryEventRepository()`. Changes made in one screen (e.g. confirming attendance in the detail) show up in the others. It is seeded from `data/demo/SampleEvents` (and organizers from `SampleUsers`), stand-ins for a future remote backend (Supabase or Firebase are planned).
- `features/<feature>/` — screens and their ViewModels (e.g. `home/HomeScreen` + `HomeViewModel`, `event/detail/EventDetailScreen`).

Navigation uses type-safe Navigation Compose: routes are `@Serializable` members of the `sealed interface MainRoutes` (`core/navigation/MainRoutes.kt`) and are read back with `entry.toRoute<...>()` in `AppNavigation.kt`. To add a screen, add a route there and a `composable<Route>` entry in the NavHost. Screens receive navigation callbacks (`onOpenEvent`, `onBack`) instead of the `NavController`.

State pattern: a ViewModel exposes a `StateFlow<XUiState>` backed by a private `MutableStateFlow`, updated with `_uiState.update { it.copy(...) }`. Screens get it with `viewModel()` and `collectAsState()`. ViewModels that read the repository collect its flows in `viewModelScope`; their unit tests need `MainDispatcherRule` (in `app/src/test`) and should create the ViewModel lazily, after the rule has set `Dispatchers.Main`.

Images load from URLs with Coil 3 (`AsyncImage`), using `res/drawable/event_placeholder.xml` as the fallback.

## Domain rule: public visibility

Only `EventStatus.VERIFIED` events may appear publicly. `EventRepository.publicEvents`/`findPublicEvent` only return verified events and the repository refuses to modify hidden ones, so `HomeViewModel` (search and category filters) and `EventDetailViewModel` (a hidden id shows as unavailable) never see them; `SampleEvents.findPublicById` applies the same rule. The sample data deliberately includes a `PENDING` event and a `REJECTED` event (`pendiente-privado`, `rechazado-privado`). `PublicEventVisibilityTest` asserts that exactly 5 events are verified and that the hidden ones are not reachable, so update that test if you change the sample data. The planned moderation flow is: new events start as `PENDING`, rejection requires a reason, and moderators can mark events `FINISHED`.

## Roadmap (from README)

The next stages are: backend for auth, data, and images; registration and login with `USER` and `MODERATOR` roles (moderators preloaded, permissions enforced in the DB); event CRUD with a map location; attendance, interests, comments, and notifications; stats, points, levels, and badges; server-verified QR check-in; and an AI category suggestion that always allows a manual override.

## Diseño

Los mockups están en `/mockups`. `mockups/README.md` indica qué pantalla corresponde a cada archivo y `mockups/DESIGN.md` es el sistema de diseño. Cada pantalla debe replicar su mockup.

## Flujo de trabajo

- Nunca hacer commit ni push a `main`.
- No hacer push sin preguntar antes al usuario.
- Al terminar cada tarea, compilar con `.\gradlew.bat assembleDebug`.
