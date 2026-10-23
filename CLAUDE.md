# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

AKJ Events is a single-module Android app (`:app`, package `co.edu.uniquindio.akjevents`) for discovering community events, built as a university class project. It is at "Fase 2": the app is functional end to end but all data lives in memory (no backend, persistence, real maps, image upload, permissions, or persistent sessions). Login checks the accounts in `InMemoryUserRepository` (demo user and two preloaded moderators, see `data/demo/DemoCredentials`); registration creates real in-memory accounts; password recovery only validates input. Code comments, UI strings, and the README are in Spanish; keep new user-facing text and comments in Spanish to match.

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

- `core/` — cross-cutting: `theme/` (AKJ Events prototype colors), shared `component/`s, `util/RequestResult` (Loading/Success/Failure for future async operations).
- `navigation/` — the main NavHost, its type-safe routes, and `SessionViewModel` (role and unread alerts for the bottom bar). Tabs are opened with `openTab`, stacked on the role's root (Home for users, Moderation for moderators). Screens with a bottom bar receive it as a `bottomBar` slot. The map picker returns its result through the previous entry's `SavedStateHandle`.
- `domain/model/` — plain Kotlin models. `CommunityEvent` enforces invariants in `init` via `require` (at least one image, `endsAt` after `startsAt`, positive capacity, `REJECTED` requires `rejectionReason`). Constructing an invalid event throws.
- `domain/repository/` + `data/repository/InMemory*Repository` — three in-memory repositories exposed as `StateFlow`s, stand-ins for a future remote backend (Supabase or Firebase are planned):
  - `UserRepository`: accounts, the session (`currentUser`, `login`, `logout`, `register`), profile edits, account deletion, and reputation (`addPoints` notifies level-ups, `awardBadge` awards once). `User.level` is derived from points (`UserLevel.forPoints`).
  - `EventRepository`: events, comments, attendances (with `checkedIn`) and interests. Management calls take the acting user id and check permissions (only the organizer edits; organizer or moderator deletes/finishes; only moderators verify/reject, and rejecting needs a reason). It also applies the community rules: points (`ReputationPoints`), badges, and notifications (e.g. a comment notifies the event author).
  - `NotificationRepository`: in-app notifications, read/unread.
  Each has a `.shared` instance wired together (no DI yet); ViewModels take repositories as constructor parameters defaulting to them. Tests build fresh, connected instances with `TestRepositories` (in `app/src/test`), which opens the session as the demo user. Seed data lives in `data/demo/` (`SampleEvents`, `SampleUsers`, `SampleParticipation`, `SamplePlaces`).
- `features/<feature>/` — screens and their ViewModels (e.g. `home/HomeScreen` + `HomeViewModel`, `event/detail/EventDetailScreen`).

Navigation uses type-safe Navigation Compose: routes are `@Serializable` members of the `sealed interface MainRoutes` (`navigation/MainRoutes.kt`) and are read back with `entry.toRoute<...>()` in `AppNavigation.kt`. To add a screen, add a route there and a `composable<Route>` entry in the NavHost. Screens receive navigation callbacks (`onOpenEvent`, `onBack`) instead of the `NavController`.

State pattern: a ViewModel exposes a `StateFlow<XUiState>` backed by a private `MutableStateFlow`, updated with `_uiState.update { it.copy(...) }`. Screens get it with `viewModel()` and `collectAsState()`. ViewModels that read the repository collect its flows in `viewModelScope`; their unit tests need `MainDispatcherRule` (in `app/src/test`) and should create the ViewModel lazily, after the rule has set `Dispatchers.Main`.

Images load from URLs with Coil 3 (`AsyncImage`, see `core/component/EventImage`), using `res/drawable/event_placeholder.xml` as the fallback. Maps are drawn by `core/component/SimulatedMap` (real lat/lng, pan/zoom, markers, center-pin picking); QR codes are real (ZXing) but scanning is simulated by typing or picking the code.

## Domain rule: public visibility

Only `EventStatus.VERIFIED` events may appear publicly. `EventRepository.publicEvents`/`findPublicEvent` only return verified events and attendance, interest and check-in only work on them, so the feed and the map never show hidden events; `SampleEvents.findPublicById` applies the same rule. In the detail, `FINISHED` events are visible but closed, and `PENDING`/`REJECTED` ones are visible only to their organizer and to moderators. New and edited events start as `PENDING`. The sample data includes pending, rejected and finished events (e.g. `pendiente-privado`, `rechazado-privado`, `reforestacion-rio`) and several owned by the demo user (`user-1`). `PublicEventVisibilityTest` and other tests assert counts from the sample data (8 verified events), so update them if you change it.

## Roadmap (from README)

The next stages are: backend for auth, data, and images; registration and login with `USER` and `MODERATOR` roles (moderators preloaded, permissions enforced in the DB); event CRUD with a map location; attendance, interests, comments, and notifications; stats, points, levels, and badges; server-verified QR check-in; and an AI category suggestion that always allows a manual override.

## Diseño

Los mockups están en `/mockups`. `mockups/README.md` indica qué pantalla corresponde a cada archivo y `mockups/DESIGN.md` es el sistema de diseño. Cada pantalla debe replicar su mockup.

## Flujo de trabajo

- Nunca hacer commit ni push a `main`.
- No hacer push sin preguntar antes al usuario.
- Al terminar cada tarea, compilar con `.\gradlew.bat assembleDebug`.

## Estado de la entrega (fase 2)

Hecho: todas las pantallas de los mockups 01 a 17 (autenticación, feed con filtro por ubicación, mapa simulado, detalle con comentarios, crear/editar evento con punto en el mapa, Mis eventos, notificaciones, perfil, editar perfil, estadísticas, check-in con QR y panel de moderación), roles Usuario/Moderador, reputación (puntos, niveles, insignias) y pruebas unitarias. La primera parte de la fase 2 ya se fusionó en `main` (PR #1); el resto está en la rama `entrega-fase2-completa`.
