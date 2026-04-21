# Emergency Ambulance Request System

Android app for emergency request coordination, built with Jetpack Compose and MVVM.

## What the app does

- Authenticates users with Sign In / Sign Up screens
- Navigates authenticated users into a bottom-navigation dashboard
- Uses a themed splash launch experience
- Reads and writes user data through Firebase Realtime Database
- Keeps mobile forms readable and usable with keyboard-aware layouts

## Architecture

- **Model**: repositories, data models, validation helpers
- **View**: Jetpack Compose screens and reusable components
- **ViewModel**: state, validation, and one-time navigation events

## Feature documentation

Each implemented area has its own documentation page:

- [Feature Documentation Index](docs/features/README.md)
- [Current Implementation Overview](docs/features/implementation-overview.md)
- [Authentication Flow](docs/features/auth.md)
- [Splash / App Launch Experience](docs/features/splash.md)
- [Dashboard & Bottom Navigation](docs/features/dashboard.md)
- [Firebase Realtime Database Integration](docs/features/firebase.md)
- [Keyboard, Back Button & Responsive UX](docs/features/ux.md)
- [Testing & Verification](docs/features/testing.md)

Supporting project documentation:

- [Project Setup Guide](docs/setup.md)
- [Database Schema](docs/schema.md)
- [GitHub Actions Pinning Guide](docs/action-pinning-guide.md)

## Key implementation highlights

### Authentication
- Shared brand header and auth container styling
- Rounded input fields with glass-style cards
- Inline navigation text for Login / Sign Up actions
- One-time ViewModel navigation events after success

### Dashboard
- Bottom navigation with `Activity`, `Requests`, and `Account`
- Full-screen background image with readable elevated placeholder cards
- Natural back-button behavior inside tab navigation

### Splash screen
- Theme-based app launch screen
- Surround color set to `#92DAFF`
- System splash configuration handled through manifest/theme resources

### Firebase
- Firebase Realtime Database-backed repository
- Indexed lookup for authentication instead of downloading the full user list
- Normalized lookup keys for email and phone

### Testing and CI
- Unit tests for model and ViewModel behavior
- Coroutine test dispatcher rule for deterministic ViewModel tests
- OWASP Dependency-Check configuration for vulnerability scanning

## Current project structure

- `app/src/main/java/...` — app source code
- `app/src/main/res/...` — layouts, colors, drawable assets, and themes
- `app/src/test/...` — unit tests
- `docs/...` — setup, schema, feature, and workflow documentation

## Running the project

See the setup guide:

- [docs/setup.md](docs/setup.md)

Typical local build commands:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Security note

Sensitive Firebase files were removed from git history and should remain untracked.
If you regenerate Firebase configuration locally, keep `google-services.json` out of version control.

## Team

AS PROPOSED BY THE TEAM
1. OTAI JOSEPH
2. ABUREK EMMANUEL
3. AKATUKUNDA PRECIOUS PRAISE
4. AGABA DORECK
5. KIBENGE VICTOR