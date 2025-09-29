# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Critical Maps is an Android app for Critical Mass bicycle protests that tracks participant locations and provides chat functionality. The app shares user locations on a map and enables communication between all participants.

## Build Commands

The project uses Gradle wrapper for building:

```bash
# Build debug APK
./gradlew assembleDebug

# Run linter
./gradlew lint

# Run unit tests
./gradlew testDebugUnitTest

# Run tests for specific class
./gradlew testDebugUnitTest --tests "*YourClassName*"

# Install debug version to connected device
./gradlew installDebug

# Clean build
./gradlew clean
```

## Architecture

### Dependency Injection
- Uses **Dagger 2** for dependency injection
- `App.java` - Application class that initializes the DaggerAppComponent
- `AppComponent.java` - Dagger component defining injection targets
- `AppModule.java` - Provides singleton instances (OkHttpClient, Picasso, SharedPreferences)
- Components are accessed via `App.components().inject(this)`

### Main Structure
- `Main.java` - Primary Activity with navigation drawer and fragment management
- Fragment-based navigation with state preservation using `FragmentProvider`
- Key fragments: MapFragment, ChatFragment, SettingsFragment, AboutFragment, RulesFragment

### Event System
- Uses **Otto event bus** for decoupled communication between components
- Event classes in `events/` package (NewLocationEvent, NetworkConnectivityChangedEvent, etc.)
- EventBus singleton provided through dependency injection

### Location & Sync
- `LocationUpdateManager` - Manages GPS location updates and permissions
- `ServerSyncService` - Background service that syncs location data and chat messages with server
- Location sharing controlled by observer mode setting and privacy policy acceptance

### Data Models
- `OwnLocationModel` - Current user's location state
- `OtherUsersLocationModel` - Locations of other users
- `ChatModel` - Chat message handling
- `UserModel` - User identification and settings
- All models are singletons managed by Dagger

### Network & Storage
- Uses **OkHttp3** for network requests (15-second timeout configured)
- **Picasso** for image loading with custom OkHttp3 downloader
- SharedPreferences for app settings with typed-preferences wrapper
- **Timber** for logging (debug tree in debug builds, no-op in release)

### Map Implementation
- Uses **osmdroid** library for OpenStreetMap integration
- Custom overlays for location markers
- GPX file support for route display

## Key Configuration

### Build Configuration
- Target SDK: 35, Min SDK: 16
- Application ID: `de.stephanlindauer.criticalmaps` (debug adds `.debug` suffix)
- Uses view binding and build config features
- ProGuard enabled for release builds
- Error-prone static analysis plugin configured

### Signing
- Release signing configuration loaded from `keystore.properties` (gitignored)
- Falls back to `dummy_keystore.properties` if not found

### Permissions
- Location permissions managed through `PermissionCheckHandler`
- Privacy policy acceptance required before location sharing
- Observer mode allows viewing without sharing location

## Testing

Unit tests are located in `app/src/test/java/` with key test classes:
- `ServerResponseProcessorTest` - Server response handling
- `ChatModelTest` - Chat functionality
- `OwnLocationModelTest` - Location model behavior
- `AeSimpleSHA1Test` - Utility testing

Instrumentation tests in `app/src/androidTest/java/` using AndroidX Test framework.

## Code Style

- Java-based codebase following Android conventions
- Uses `@Inject` annotations for dependency injection
- Timber for logging instead of Log class
- ViewBinding for UI component access
- Null safety with androidx.annotation.NonNull/Nullable

## Handler Pattern

The codebase uses a handler pattern for complex operations:
- `handler/` package contains specialized handlers for specific actions
- Examples: `GetLocationHandler`, `PostChatmessagesHandler`, `ImageUploadHandler`
- Handlers are typically instantiated and executed from UI components