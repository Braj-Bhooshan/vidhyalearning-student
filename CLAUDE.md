# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Android app (Kotlin + Jetpack Compose) called "StudentProfile" (`com.studentprofile.app`) — a parent-facing
mobile client for the multi-tenant school SaaS platform. Despite screen names like "Student Dashboard",
this is **not a separate student role**: it's a single parent-login → pick-child → view-that-child's-data
flow. There is no student-only mode.

It is a from-scratch reimplementation of the parent app in `C:\Users\brajs\StudioProjects\vidhyalearning`
(package `com.example.vidyalearning`), which should be treated as **read-only reference** — when adding a
screen/feature here, check the equivalent screen there first (same architecture layers: `data/`, `domain/`,
`presentation/`) rather than designing from scratch. Do not edit that project.

Backend is the FastAPI service at `D:\Raja\school logo\saas_school\backend` — also **read-only** from this
project. It has its own `CLAUDE.md` with full endpoint/service documentation. If a feature here needs a
backend change (new endpoint, new field), do not edit the backend directly as a side effect of an app
change — instead produce a clear written list of the exact endpoint(s)/fields needed (method, path,
request/response shape) so the backend can be updated manually by its owner.

## Build / run

```
./gradlew assembleDebug          # build debug APK
./gradlew installDebug           # build and install on connected device/emulator
./gradlew test                   # unit tests (app/src/test)
./gradlew connectedAndroidTest   # instrumented tests (app/src/androidTest)
./gradlew testDebugUnitTest --tests "com.studentprofile.app.SomeClassTest"   # single test class
```

No lint/ktlint config is present beyond the default Android Gradle plugin lint (`./gradlew lint`).

## Backend connectivity (local dev)

The backend is expected reachable at `127.0.0.1:8002` (API gateway) — see `TenantProvider.getBaseUrl()`.
Multi-tenancy is header-based, not subdomain-based, at the network layer: `AuthInterceptor`
(`data/remote/AuthInterceptor.kt`) rewrites every outgoing request's host/port to `127.0.0.1:8002` and
attaches `X-Tenant-Subdomain` + a synthetic `Host: <subdomain>.localtest.me` header, using the subdomain
saved in `TenantProvider` during the subdomain-verification step. Storage/media paths (`/media/`,
`/static/`, `/uploads/`, `/storage/`, or host `minio`) are routed the same way but without the tenant
headers, since they go through the same gateway. The `Authorization: Bearer <token>` header is attached to
everything except login/access-token endpoints and storage requests.

## Auth flow (`presentation/viewmodel/AuthViewModel.kt`)

Multi-step state machine driven by `AuthState` (`presentation/viewmodel/AuthState.kt`):

1. `SubdomainRequired` — user enters school subdomain; `verifySubdomain()` hits `SchoolApi` to confirm the
   tenant exists and caches subdomain/school name/logo in `TenantProvider`.
2. `Unauthenticated` — parent logs in with email/password (`AuthApi.login`), which fetches
   `StudentAuthApi.getMyChildren()` (`/api/v1/auth/student/my-children`) to list linked children.
3. `StudentSelectionRequired` — parent picks a child.
4. `MPINRegistrationRequired` / `MPINLoginRequired` — depending on whether that child already has an MPIN
   set up, either `setupMpin()` or `verifyMpin()` is called against `/api/v1/auth/student/mpin/*`. Both
   return a **student-scoped access token** that replaces the parent token in `TenantProvider` for all
   subsequent requests — i.e. after this step the app is authenticated *as the selected child*, not the
   parent.
5. `Authenticated` — lands on `StudentMainScreen`.

`ParentRepository` persists parent/children data as hand-rolled JSON in SharedPreferences (not a real
cache/DB layer) — it's a local index, not a source of truth; always re-fetch from the API where fresh data
matters. Session is intentionally **not** persisted across process death: `AuthViewModel.restoreSession()`
clears all prefs/tenant data on every ViewModel init, so the app always starts at `SubdomainRequired`.

## Navigation architecture

Two-level navigation, **not** using `NavHost` route strings for the main app:

- `AppNavigation.kt` — top-level `NavHost` with exactly two destinations: `auth` (`AuthGate`) and
  `student_dashboard` (`StudentMainScreen`).
- `StudentMainScreen.kt` — everything inside the dashboard is a manual `screenStack: List<String>` (a
  hand-rolled back stack of screen-name strings switched on in a big `when`), not Compose Navigation. Use
  `navigate("ScreenName")` / `back()` / `switchTab("ScreenName")` conventions already established there
  when adding new destinations reachable from inside the dashboard, rather than introducing `NavHost`
  routes. Bottom nav tabs (`Dashboard`, `Classes`, `Assignment`, `Message`) reset the stack via
  `switchTab`; everything else pushes onto it via `navigate`.
- Video playback (`ClassesViewModel.videoState`) is hoisted to `StudentMainScreen` root level as an overlay
  (not a stack entry) so the player and its moving watermark cover the bottom nav too — see
  `VideoPlayerScreen` / `WatermarkOverlay`. `FLAG_SECURE` is expected to stay set while any video overlay
  is showing; don't remove it when touching that flow.

## API/DI layer

- Every remote API interface lives in `data/remote/*Api.kt` (Retrofit interfaces) and is wired as a
  `@Provides @Singleton` in `di/AppModule.kt` — there's one Retrofit instance and one OkHttpClient shared by
  all APIs. When adding a new API, add both the interface and its provider function there.
- `domain/models/` holds the Kotlin data classes for API DTOs. There's no separate network-DTO vs
  domain-model split — the Retrofit `@Serializable` classes in `domain/models/` are used directly by
  ViewModels and Compose screens.
- Screens generally take a `studentId: Int` (parsed from `AuthViewModel.selectedStudentDetails`) plus a
  standard set of chrome callbacks (`onMenuClick`, `onNotificationClick`, `onProfileClick`,
  `onLogoutClick`) — follow that existing parameter shape for new screens so they compose into
  `StudentMainScreen`'s `when` block without special-casing.

## Verified vs assumed API contracts

Some endpoints here were confirmed against the backend's `/openapi.json`; others were implemented against
an assumed shape and haven't been checked against the backend owner. When touching auth, classes,
attendance, academic-year, grades/examination, or assignment endpoints, check whether the contract is
already verified before assuming the request/response shape — search this project's memory or the backend
`CLAUDE.md`'s endpoint table before guessing.
