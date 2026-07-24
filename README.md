# Weather — Offline-First Android Weather App

A production-style Android weather app built with Jetpack Compose. It shows **current
conditions, an hourly forecast (line chart), and a 7-day forecast, works fully offline
(Room is the single source of truth), syncs in the background, refreshes
intelligently via TTL, and re-themes the whole screen based on the current weather
condition**.

Weather data comes from [WeatherAPI.com](https://www.weatherapi.com/docs/).

---

## Features

| # | Feature | Where |
|---|---|---|
| 1 | Jetpack Compose UI — deliberately minimal (temperature, condition, high/low, simple hourly & 7-day lists) with a dynamic, condition-driven look | `presentation/weather` |
| 2 | Networking via **Retrofit** (+ OkHttp + kotlinx.serialization) | `data/remote` |
| 3 | Local **Room** cache, one row-set per city with a fetch timestamp | `data/local` |
| 4 | **Coroutines + Flow** end to end (DB `Flow`s drive the UI) | everywhere |
| 5 | **Periodic background sync** via WorkManager | `data/sync` |
| 6 | **Offline-first** — the DB is the single source of truth | `data/repository` |
| 7 | **Unit tests** — a focused JVM suite (incl. a Robolectric worker test) | `src/test` |
| 8 | **Smart refresh** — network calls only when the cache is stale (TTL) | `domain/policy`, `data/repository` |
| 9 | **Dynamic theming** — gradient + Material color scheme derived from the weather | `presentation/theme` |

---

## Tech stack

| Concern | Choice |
|---|---|
| UI | Jetpack Compose (Material 3) — minimal, text-first |
| Networking | Retrofit + OkHttp + `retrofit2-kotlinx-serialization-converter` |
| Serialization | KotlinX Serialization |
| Local storage | Room (KSP) |
| Concurrency | Coroutines + Flow |
| DI | Koin (incl. `koin-androidx-workmanager`) |
| Background work | WorkManager |
| Navigation | Type-safe Compose Navigation |
| Testing | JVM only — JUnit 5, Turbine, AssertK, `kotlinx-coroutines-test`, MockWebServer, and Robolectric + WorkManager-testing for the worker |

---

## Architecture

**Single-module, package-by-layer** clean architecture. Layers are top-level packages and
the dependency rule `presentation → domain ← data` is enforced by package discipline (the
`domain` package is pure Kotlin with no Android imports; ViewModels depend only on the
`WeatherRepository` interface; `di/` wires everything — the single-module stand-in for
`:app`).

```
              ┌────────────────────────────────────────────┐
              │                presentation                 │
              │  Compose screens · MVI ViewModels · theme   │
              └───────────────┬────────────────────────────┘
                              │ depends on
              ┌───────────────▼────────────────────────────┐
              │                   domain                    │
              │  models · WeatherRepository (interface)     │
              │  RefreshPolicy  (pure Kotlin)               │
              └───────────────▲────────────────────────────┘
                              │ implements
              ┌───────────────┴────────────────────────────┐
              │                    data                     │
              │  Retrofit remote  ·  Room local (SSOT)      │
              │  OfflineFirstWeatherRepository  ·  sync     │
              └─────────────────────────────────────────────┘

     core/  (cross-cutting): Result/Error/DataError · Clock · UiText
     di/    (Koin modules) · WeatherApp (Application) · MainActivity (NavHost)
```

**MVI presentation** — every screen has `State`, `Action`, `Event`, a `ViewModel` exposing
`StateFlow<State>` and a `Channel<Event>`, and a `Root` (holds the ViewModel, observes
events) / `Screen` (pure `(state, onAction)`, previewable) composable split.

---

## Design decisions & rationale

Every notable choice, and why:

### Single-module, package-by-layer (not multi-module)

### Retrofit for networking
`safeApiCall` wrapper that maps exceptions/HTTP codes to `DataError.Network`, DTO ↔ domain ↔
entity separation with extension-function mappers, and an offline-first repository. An
`ApiKeyInterceptor` appends the `key` query parameter so the API interface never touches the
secret. The kotlinx.serialization converter keeps DTOs `@Serializable`.

### One `forecast.json` call for everything — `current.json` is **not** used
A single `GET /v1/forecast.json?q=<loc>&days=7&alerts=no&aqi=no` returns `current` **and** hourly
**and** the 7-day forecast in one payload. Using a separate `current.json`
was explicitly rejected because it would mean:
- **Two round-trips instead of one** — wasteful and at odds with the "refresh only when
  needed" / TTL goal, and it doubles free-tier quota usage per refresh.
- **Consistency risk** — `current` from call A and forecast from call B can be captured
  seconds apart; folding them into one atomic Room transaction with a single `lastFetchedEpoch`
  is cleaner for an offline-first "single source of truth".
- **Simpler invalidation** — one fetch → one timestamp → one TTL decision per city.

  *(The only case where splitting helps is wanting current on a much shorter TTL than the
  forecast; that granularity isn't needed here.)*


### Location = `auto:ip` on first launch + city search
On first launch the app resolves an approximate location via WeatherAPI's
`q=auto:ip` (GeoIP on the device's public IP) — **no runtime location permission**. The
resolved coordinates are then **pinned as the city's stable Room key (`"lat,lon"`)**, *not*
the literal `auto:ip` because pin: `auto:ip` drifts as the IP/network changes (different Wi-Fi,
VPN, CGNAT), which would fragment the cache and confuse TTL; pinning to `lat,lon` makes "My
Location" a stable, cacheable, background-syncable city like any other. Caveats: GeoIP is
approximate and reflects the IP at first resolution (it does not track movement), and it needs one online moment before the first
cache exists.

### Multiple saved cities, each cached separately
Every city is cached in Room with its own `lastFetchedEpoch`, matching the "cache
per city with timestamp" requirement, and background sync refreshes them all. The home screen
is a `HorizontalPager` over the saved cities; the "Manage cities" screen adds/removes them via
search.

### Other choices
- **DI = Koin** — works cleanly in a single module and provides
  `koin-androidx-workmanager` for injecting dependencies into the sync worker.
- **JUnit 5 test stack** (Turbine, AssertK, coroutines-test, MockWebServer)
- **WorkManager for periodic sync** — the standard, battery-friendly Android scheduler with
  network constraints and backoff.
- **Minimal, text-first UI (no charts, no weather graphs)** — the screen shows only the
  essentials: current temperature, condition text, today's high/low, a simple hourly
  time+temperature strip, and a 7-day high/low list. The condition still drives the
  gradient/color theme, which conveys the weather them and works fully offline.

---

## How it works (data flow)

**Offline-first, Room as the single source of truth.** The ViewModel only ever observes
the database. Refreshes fetch from the network and persist; the DB `Flow` then re-emits and
the UI updates.

```
 UI (Compose) ──observes──► WeatherViewModel ──observes──► Room Flows  ◄─── DB is Single Source of truth
                                   │                          ▲
                                   │ refresh(cityId, force)   │ atomic write (one txn)
                                   ▼                          │
                     OfflineFirstWeatherRepository ──► Retrofit (forecast.json)
                                   │                          │
                             RefreshPolicy (TTL)
                            skip network if fresh
```

- **Smart refresh (TTL).** `RefreshPolicy` (default 30 min) decides staleness from
  `lastFetchedEpoch` vs. an injectable `Clock`. Opening a city refreshes only if stale;
  pull-to-refresh forces a fetch. On network failure the cache is kept and a "no internet"
  snackbar is shown — the cached weather stays on screen.
- **Background sync.** `WeatherSyncWorker` (a `CoroutineWorker`, Koin-injected) runs every
  90 min, refreshing all saved cities under a `CONNECTED` constraint with exponential backoff,
   so caches never rot.
- **Dynamic theming.** `WeatherPalette.forCondition(type, isDay)` maps the current condition
  to a gradient + Material `ColorScheme`. `WeatherBackground` cross-fades the gradient with
  `animateColorAsState` read inside `drawBehind` (draw phase), so theme changes don't
  recompose the content on top.

---

## Room schema

foreign keys cascade from `cities`, reads exposed as `Flow`.

| Table | Key | Purpose |
|---|---|---|
| `cities` | `id` (`"lat,lon"` or search `url` slug) | saved city + `lastFetchedEpoch` (drives TTL) |
| `current_weather` | `cityId` | current conditions |
| `hourly_forecast` | auto id, `cityId` FK | hourly rows |
| `daily_forecast` | auto id, `cityId` FK | daily rows |

A `withTransaction` in `RoomWeatherLocalDataSource.replaceWeather` writes all of a city's
weather atomically and stamps `lastFetchedEpoch`.

---

## Project structure

```
com.example.weatherapp
├── WeatherApp.kt              Application: startKoin + WorkManager factory + schedule sync
├── MainActivity.kt            Edge-to-edge host, NavHost
├── core/
│   ├── domain/util/           Result/Error/DataError, map/onSuccess/onFailure, Clock
│   ├── presentation/          UiText, ObserveAsEvents, DataError.toUiText()
├── domain/
│   ├── model/                 City, CityWeather, CurrentWeather, HourlyForecast, DailyForecast,
│   │                          WeatherCondition
│   ├── repository/            WeatherRepository (interface)
│   ├── policy/                RefreshPolicy (TTL)
│   └── error/                 WeatherError
├── data/
│   ├── remote/                WeatherApi, ApiKeyInterceptor, safeApiCall, dto/, mapper/
│   ├── local/                 WeatherDatabase, dao/, entity/, mapper/, safeDbCall
│   ├── datasource/            Retrofit remote & Room local data sources
│   ├── repository/            OfflineFirstWeatherRepository
│   └── sync/                  WeatherSyncWorker, SyncScheduler
├── presentation/
│   ├── theme/                 WeatherAppTheme, WeatherPalette, Color, Type
│   ├── weather/               WeatherScreen (Root+Screen), MVI, UI models, components/
│   ├── cities/                CitiesScreen (Root+Screen), MVI
│   └── navigation/            type-safe routes + NavGraph
└── di/                        Koin modules (network, database, preferences, data, presentation, worker)
```

---

## Setup & running

1. **API key.** The WeatherAPI key is read from `local.properties` into `BuildConfig` (not
   hardcoded in VCS):
   ```properties
   WEATHER_API_KEY=your_key_here
   ```
   A demo key is bundled as a fallback in `app/build.gradle.kts`, so the app builds and runs
   out of the box.

2. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run** on a device/emulator with internet. On first launch the app resolves your
   approximate location (`auto:ip`) and shows its weather; use the top-right **Manage cities**
   button to search and add more cities, then swipe between them in main screen.

**Permissions:** `INTERNET` and `ACCESS_NETWORK_STATE` only.

---

## Testing

All tests run on the **JVM** — no device or emulator required:

```bash
./gradlew testDebugUnitTest
```

A small suite covering the app's core logic. Logic is tested with JUnit 5; the WorkManager
worker is tested with **Robolectric** (JUnit 4, run under the JUnit Platform via the vintage
engine).

- `OfflineFirstWeatherRepositoryTest` — the heart of the app: skip-when-fresh, force,
  stale-fetch (**smart TTL refresh**), **cache preserved on network failure**, auto-location
  seeding, add/remove/search.
- `WeatherViewModelTest` (fake repository, Turbine) — loading→content, refresh targeting,
  error→snackbar event, navigation events.
- `WeatherSyncWorkerRobolectricTest` — `TestListenableWorkerBuilder` under Robolectric:
  success → `Result.success`, network failure → `Result.retry`.

> Robolectric downloads its Android runtime jar on first run, so the initial test execution
> needs network access.

---

## Assumptions & limitations
- No severe-weather alerts as it's mentioned optional in technical requirements of doc.
  The app will not warn you about a hurricane.
- **`alerts=no` and `aqi=no` are sent explicitly**, and only the fields the app actually
  renders are declared in the DTOs; everything else in the payload is skipped by
  `ignoreUnknownKeys`.
- **`auto:ip` is approximate** (GeoIP) and reflects the IP at first resolution; it does not
  track movement, and can be off on VPN. First launch needs one online moment before a
  cache exists.
- **Metric only** (°C / km/h / mm).
- **Times are formatted in the device's timezone** from the API epoch fields (kept minimal —
  the localized per-location time string isn't stored).
- **WorkManager minimum period is 15 min**; the app uses 90 min by default.
- **HTTP 400** from the forecast endpoint is treated as "location not found" (WeatherAPI
  returns code 1006 for unknown locations).
```
