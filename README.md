<div align="center">

<img src="docs/assets/knt-logo.png" alt="K&T Transport" width="260"/>

# K&T Transport — Android App

### *Moving Communities Forward*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26%20(Android%208.0)-blue?style=flat-square)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green?style=flat-square)](https://developer.android.com)
[![Version](https://img.shields.io/badge/Version-1.0.0-E8C14A?style=flat-square)](https://github.com)

---

**A premium community transport booking app serving Beacon Valley and Mitchell's Plain, Cape Town.**  
Book single trips, subscribe to lift clubs, track your rides in real time — all in one place.

</div>

---

## Table of Contents

- [Overview](#overview)
- [Screenshots](#screenshots)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Build & Run](#build--run)
- [Backend Integration](#backend-integration)
- [Roadmap](#roadmap)
- [Developer](#developer)

---

## Overview

K&T Transport is a fully native Android app built for the commuter community of Beacon Valley and Mitchell's Plain. It connects commuters directly with K&T's verified drivers — no third-party platforms, no hidden fees.

The app covers the full journey lifecycle:

```
Commuter books trip  →  K&T sends quote  →  Commuter accepts  →  Driver confirmed  →  Trip complete
```

For recurring commuters, **Lift Clubs** allow groups to subscribe to a shared route — once the quota is filled, a driver is assigned and the cost is shared.

---

## Features

### 🚌 Trip Booking
- Request a one-way trip with pickup address, drop-off, date, time and passenger count
- Receive and review a quote directly in the app
- Accept the quote to confirm — driver name, vehicle and plate number revealed
- Live trip status pipeline: **Pending → Quoted → Accepted → Confirmed → En Route → Done**
- Real-time countdown timer to pickup for confirmed trips
- Receipt view for completed trips
- Rate your driver with 1–5 stars and a comment

### 👥 Lift Clubs
- Browse available shared lift clubs by route
- Subscribe to join a club — once the quota is met, K&T assigns a driver
- Create your own lift club request for your commute
- Monthly, weekly, and fortnightly payment cycle support

### 🔔 Notifications
- Push notification infrastructure (FCM-ready, activate with `google-services.json`)
- In-app notification centre with unread badge counter
- Swipe right to mark as read · Swipe left to dismiss
- Quote received, trip confirmed, lift club updates

### 👤 Profile & Settings
- Full profile management — name, email, phone number
- Profile photo: choose from gallery, take a photo, or use initials avatar
- Dark mode, Light mode, System default theme selector with mini phone preview illustrations
- Sign out with confirmation dialog

### ✨ UX & Polish
- Animated hero headers with real Cape Town photography
- Floating user avatar on the home screen overlapping the hero photo
- Staggered list animations, spring-bounce card press interactions
- Shimmer skeleton loading states
- Parallax-collapsing home header on scroll
- Pulsing notification badge
- Animated sliding pill bottom navigation
- Glassmorphism card styling
- Gradient text on key headings
- Animated stat counters
- Screen-to-screen slide transitions

### 🔐 Security & Production Readiness
- JWT stored in `EncryptedSharedPreferences` (AES256-GCM)
- Retrofit + OkHttp network layer with auth interceptor
- R8/ProGuard enabled in release builds with resource shrinking
- `ACCESS_NETWORK_STATE`-backed live connectivity banner
- `PasswordStrengthIndicator` and SA phone/email validation
- Official Android 12+ Splash Screen API
- `BuildConfig` fields for dev/prod API URL switching

### 🚀 Onboarding
- 3-page swipeable intro for first-time users (shown once, persisted via DataStore)
- Forgot Password flow with email validation and success confirmation state

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.10 |
| UI Framework | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose 2.9.7 |
| Networking | Retrofit 2.11 + OkHttp 4.12 |
| Image Loading | Coil 3.1 |
| Security | EncryptedSharedPreferences (AES256-GCM) |
| Persistence | DataStore Preferences |
| Paging | Jetpack Paging 3 |
| Fonts | Syne (headings) · DM Sans (body) |
| Build System | Gradle (Kotlin DSL) + AGP 9.1.0 |
| Min / Target SDK | 26 / 36 |

---

## Architecture

The app follows a **layered MVVM architecture**, API-ready with `SampleData` as a mock layer until the Spring Boot backend is connected:

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  Compose Screens + Components       │
│  AppColors · KntScaffold · KntCard  │
└──────────────┬──────────────────────┘
               │ observes
┌──────────────▼──────────────────────┐
│         ViewModel Layer             │
│  AuthViewModel · TripViewModel      │
│  StateFlow<ApiResult<T>>            │
└──────────────┬──────────────────────┘
               │ calls
┌──────────────▼──────────────────────┐
│        Repository Layer             │
│  AuthRepository · TripRepository   │
│  UserRepository · NotifRepository  │
└──────────────┬──────────────────────┘
               │ uses
┌──────────────▼──────────────────────┐
│         Network Layer               │
│  ApiClient (Retrofit + OkHttp)      │
│  ApiService · AuthInterceptor       │
│  TokenManager (EncryptedPrefs)      │
└─────────────────────────────────────┘
```

`ApiResult<T>` sealed class wraps every network call:
```kotlin
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = 0) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}
```

---

## Project Structure

```
app/src/main/java/com/kntransport/app/
│
├── data/
│   └── Models.kt              # Local data classes + SampleData
│
├── network/
│   ├── ApiClient.kt           # Retrofit singleton
│   ├── ApiService.kt          # Retrofit interface
│   ├── ApiModels.kt           # DTOs (SerializedName annotations)
│   ├── ApiResult.kt           # Sealed result wrapper + safeApiCall
│   ├── AuthInterceptor.kt     # JWT Bearer header injection
│   ├── TokenManager.kt        # EncryptedSharedPreferences token storage
│   └── ConnectivityMonitor.kt # Flow<Boolean> network state
│
├── repository/
│   ├── AuthRepository.kt
│   ├── TripRepository.kt
│   ├── UserRepository.kt
│   └── NotificationRepository.kt
│
├── viewmodel/
│   ├── AuthViewModel.kt
│   └── TripViewModel.kt
│
├── navigation/
│   └── Navigation.kt          # NavHost with animated transitions
│
├── ui/
│   ├── components/
│   │   └── Components.kt      # KntScaffold, KntCard, UserAvatar,
│   │                          # NetworkBanner, ErrorState,
│   │                          # PasswordStrengthIndicator, ShimmerBox…
│   ├── screens/
│   │   ├── SplashScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── SignUpScreen.kt
│   │   ├── ForgotPasswordScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── BookTripScreen.kt
│   │   ├── MyTripsScreen.kt
│   │   ├── TripDetailScreen.kt
│   │   ├── RateTripScreen.kt
│   │   ├── LiftClubsScreen.kt
│   │   ├── LiftClubDetailScreen.kt
│   │   ├── CreateLiftClubScreen.kt
│   │   ├── QuoteReviewScreen.kt
│   │   ├── NotificationsScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── EditProfileScreen.kt
│   │   └── AppearanceScreen.kt
│   └── theme/
│       ├── Color.kt           # DarkAppColors + LightAppColors
│       ├── Theme.kt           # KNTTransportTheme, ThemeMode
│       └── Type.kt            # Syne + DM Sans typography
│
├── KntApplication.kt          # Application class — ApiClient init,
│                              # notification channels
├── KntMessagingService.kt     # FCM handler (activate when ready)
└── MainActivity.kt            # SplashScreen API, DataStore onboarding
```

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or newer
- JDK 21
- Android SDK with API 36

### Clone & Open

```bash
git clone https://github.com/mr-h-digital/knt-trip-booking-app-android-frontend.git
```

Open Android Studio → **File → Open** → select the cloned folder. Wait for Gradle sync to complete.

### Local configuration

The app runs entirely on `SampleData` out of the box — no backend required to run the UI.

When connecting to the Spring Boot backend, update the API URL in `app/build.gradle.kts`:

```kotlin
// Debug — Android emulator localhost
buildConfigField("String", "ACTIVE_API_URL", "\"http://10.0.2.2:8080/\"")

// Release — Railway-hosted API
buildConfigField("String", "ACTIVE_API_URL", "\"https://your-app.railway.app/\"")
```

---

## Build & Run

### Debug (emulator or USB device)

```bash
./gradlew installDebug
```

Or press **Run ▶** in Android Studio with a device/emulator selected.

### Release APK

```bash
./gradlew assembleRelease
```

> R8 minification and resource shrinking are enabled in release. ProGuard rules are in `app/proguard-rules.pro`.

### Enable Push Notifications (FCM)

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add your Android app (`com.kntransport.app`) and download `google-services.json`
3. Place `google-services.json` in the `/app` directory
4. Uncomment the plugin line in `app/build.gradle.kts`:
   ```kotlin
   id("com.google.gms.google-services")
   ```
5. Add the FCM dependency:
   ```kotlin
   implementation("com.google.firebase:firebase-messaging-ktx:24.1.1")
   ```
6. Uncomment the service class in `KntMessagingService.kt`
7. Register the service in `AndroidManifest.xml`

---

## Backend Integration

The frontend is architected and ready for the Spring Boot API — only the data source layer needs to change.

| Concern | Current | Production |
|---|---|---|
| Data source | `SampleData` (in-memory) | Retrofit API calls |
| Auth | No-op | JWT from `POST /api/auth/login` |
| Token storage | — | `EncryptedSharedPreferences` via `TokenManager` |
| Role routing | Static → Home | JWT claims → COMMUTER / DRIVER / ADMIN |

### Planned backend stack

| Component | Technology |
|---|---|
| Framework | Spring Boot (Maven) |
| Database | PostgreSQL |
| Hosting | Railway |
| Auth | Spring Security + JWT |

### Switching a screen from SampleData to API

```kotlin
// Before (SampleData)
val trips = SampleData.myTrips

// After (ViewModel + StateFlow)
val viewModel: TripViewModel = viewModel()
val state by viewModel.trips.collectAsState()
LaunchedEffect(Unit) { viewModel.loadTrips() }

when (val s = state) {
    is ApiResult.Loading  -> ShimmerList()
    is ApiResult.Error    -> ErrorState(s.message, onRetry = { viewModel.loadTrips() })
    is ApiResult.Success  -> TripList(s.data)
}
```

---

## Roadmap

- [ ] Connect to Spring Boot REST API (Railway)
- [ ] Driver dashboard screen
- [ ] Admin panel — manage drivers, bookings, send quotes
- [ ] Room database caching layer (add after API is live)
- [ ] Firebase Crashlytics + Analytics
- [ ] In-app update (Play Core)
- [ ] WebSocket real-time trip status (Spring STOMP)
- [ ] Receipt PDF export / share sheet
- [ ] Biometric / fingerprint login

---

<br/>

---

<div align="center">

<br/>

*Designed and developed by*

<br/>

<img src="docs/assets/mrh-digital-logo.png" alt="Mr H Digital" width="200"/>

<br/>

**Mr H Digital**

*Digital Solutions for Growing Businesses*

<br/>

[![Website](https://img.shields.io/badge/Website-mrhdigital.co.za-84CC16?style=flat-square)](https://mrhdigital.co.za)
[![Email](https://img.shields.io/badge/Email-info%40mrhdigital.co.za-84CC16?style=flat-square)](mailto:info@mrhdigital.co.za)

<br/>

*© 2026 Mr H Digital. All rights reserved.*

</div>
