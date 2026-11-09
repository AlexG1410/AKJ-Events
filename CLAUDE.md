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
- `domain/repository/` + `data/repository/InMemory*Repository` — in-memory repositories exposed as `StateFlow`s, stand-ins for a future remote backend (Supabase or Firebase are planned). They only store and query data; they never award points, send notifications or check permissions:
  - `UserRepository`: accounts, the session (`currentUser`, `login`, `logout`, `register`), profile edits, account deletion, and reputation data (`addPoints` returns a `PointsChange`, `addBadge` returns whether it was new). `User.level` is derived from points (`UserLevel.forPoints`).
  - `EventRepository`: events, comments, attendances (with `checkedIn`) and interests; `updateStatus`, `registerCheckIn`, `registerVerificationReward`. Capacity checks stay here because they must be atomic.
  - `NotificationRepository`: in-app notifications, read/unread.
  - `RecommendationRepository` (`LocalRecommendationRepository` + `LocalRecommendationRanker`).
- `domain/usecase/<area>/` — one class per action with `@Inject constructor` and `operator fun invoke` (guide 12 of the course). They hold the community rules: permissions (only the organizer edits; organizer or moderator deletes/finishes; only moderators verify/reject, and rejecting needs a reason), points (`ReputationPoints`, via `AwardPointsUseCase`, which skips moderators and notifies level-ups), badges (`AwardBadgeUseCase`), and notifications (e.g. `AddCommentUseCase` notifies the event author). New and edited events are always saved `PENDING` by `CreateEventUseCase`/`UpdateEventUseCase`.
- `di/RepositoryModule` — Hilt `@Binds` from each repository interface to its in-memory `@Singleton` implementation. `AKJEventsApp` is the `@HiltAndroidApp`, `MainActivity` is `@AndroidEntryPoint`. In-memory repositories have an `@Inject constructor()` with the seed data and a primary constructor for tests.

ViewModels are `@HiltViewModel` and depend only on use cases (never on repositories). Screens get them with `hiltViewModel()` (from `androidx.hilt.lifecycle.viewmodel.compose`). ViewModels that need route arguments (`EventDetail`, `EditEvent`, `CheckIn`, `PickLocation`) read them from `SavedStateHandle` in their `@Inject` secondary constructor; their primary constructor takes the values directly so tests can call it. The same pattern provides test-only parameters (`now`, `simulatedRequestMs`). Tests build fresh, connected repositories with `TestRepositories` and their use cases with `TestUseCases` (both in `app/src/test`); the session opens as the demo user. Seed data lives in `data/demo/` (`SampleEvents`, `SampleUsers`, `SampleParticipation`, `SamplePlaces`).
- `features/<feature>/` — screens and their ViewModels (e.g. `home/HomeScreen` + `HomeViewModel`, `event/detail/EventDetailScreen`).

Navigation uses type-safe Navigation Compose: routes are `@Serializable` members of the `sealed interface MainRoutes` (`navigation/MainRoutes.kt`) and are read back with `entry.toRoute<...>()` in `AppNavigation.kt`. To add a screen, add a route there and a `composable<Route>` entry in the NavHost. Screens receive navigation callbacks (`onOpenEvent`, `onBack`) instead of the `NavController`.

State pattern: a ViewModel exposes a `StateFlow<XUiState>` backed by a private `MutableStateFlow`, updated with `_uiState.update { it.copy(...) }`. Screens get it with `hiltViewModel()` and `collectAsState()`. ViewModels that observe data collect the flows returned by `Observe*UseCase`s in `viewModelScope`; their unit tests need `MainDispatcherRule` (in `app/src/test`) and should create the ViewModel lazily, after the rule has set `Dispatchers.Main`.

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

Hecho: todas las pantallas de los mockups 01 a 17 (autenticación, feed con filtro por ubicación, mapa simulado, detalle con comentarios, crear/editar evento con punto en el mapa, Mis eventos, notificaciones, perfil, editar perfil, estadísticas, check-in con QR y panel de moderación), roles Usuario/Moderador, reputación (puntos, niveles, insignias) y pruebas unitarias. La fase 2 completa ya está en `main`.

Correcciones del profesor a la fase 2 (rama `correcciones-hilt-casos-de-uso`): se pidió usar Hilt y casos de uso como en la guía 12, y sacar del repositorio de eventos las reglas (puntos, notificaciones). Hecho: Hilt + KSP, `RepositoryModule`, casos de uso en `domain/usecase/` y ViewModels con `@HiltViewModel`.

Fase 3 (entrega final): las guías del curso están en https://caflorezvi.github.io/guias-apps-moviles/ (Room, DataStore, strings/i18n, permisos, Mapbox, Retrofit, Firestore, Firebase Auth y APK release). Hay que seguir sus patrones.
