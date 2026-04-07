# Authentication Flow

## Overview

The app uses a Compose-based authentication flow for **Sign In** and **Sign Up**. The authentication screens are built with shared UI components and follow the MVVM pattern:

- **View**: `SignInScreen.kt`, `SignUpScreen.kt`
- **ViewModel**: `SignInViewModel.kt`, `SignUpViewModel.kt`
- **Repository**: `FirebaseUserRepository.kt`

Successful authentication navigates the user into the dashboard area.

## User Experience

### Sign In
- Users can sign in using email or phone number plus password.
- The login form is displayed in a glass-style elevated card.
- The sign-in screen shows the app brand/logo at the top.
- A clickable inline `Sign Up` text is shown under the form.

### Sign Up
- Users can create a new account with:
  - full name
  - phone number
  - email
  - password
  - confirm password
- The sign-up form is also displayed in a glass-style elevated card.
- The brand/logo appears above the form.
- A clickable inline `Login` text is shown under the form.

## UI Components Used

Shared auth UI is split into reusable components:

- `AuthScreenContainer` — shared background + centered auth layout
- `AuthInputField` — shared rounded text field component
- `BrandHeader` — shared top logo/brand element

## Navigation Behavior

After successful sign-in or sign-up:
- the app navigates to the main dashboard route,
- the auth screens are removed from the back stack,
- and the dashboard becomes the new entry point for the session.

## Validation Rules

Validation is handled in the ViewModels and domain validator helpers.
Examples include:

- non-empty email or phone input for sign in,
- valid email/phone format,
- password presence and complexity checks,
- password confirmation matching on sign up.

## Important Notes

- The auth flow currently uses a Firebase Realtime Database-backed repository.
- Authentication is implemented with direct repository checks against stored user records.
- The codebase uses one-time navigation events so success does not rely on transient UI messages.
- The auth screens are scrollable and IME-aware so fields remain visible when the keyboard opens.

## Related Files

- `app/src/main/java/com/example/mobiledev/feature/signin/presentation/SignInScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signin/presentation/SignInViewModel.kt`
- `app/src/main/java/com/example/mobiledev/feature/signup/presentation/SignUpScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signup/presentation/SignUpViewModel.kt`
- `app/src/main/java/com/example/mobiledev/ui/components/AuthScreenContainer.kt`
- `app/src/main/java/com/example/mobiledev/ui/components/AuthInputField.kt`
- `app/src/main/java/com/example/mobiledev/ui/components/BrandHeader.kt`
- `app/src/main/java/com/example/mobiledev/navigation/NavGraph.kt`

