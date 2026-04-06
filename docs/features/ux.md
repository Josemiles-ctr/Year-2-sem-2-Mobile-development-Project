# Keyboard, Back Button & Responsive UX

## Overview

The app includes several user-experience improvements to keep forms usable on mobile devices and to make navigation feel natural.

## Keyboard Handling

The authentication screens are configured so the keyboard does not cover input fields.
The implementation combines:

- `adjustResize` on the activity window,
- `imePadding()` on auth content,
- scrollable auth containers.

This ensures the user can continue typing without losing access to lower form fields.

## Rounded / Glass UI

The auth screens use:

- rounded text fields,
- translucent card surfaces,
- subtle borders,
- elevated containers for readable text over image backgrounds.

This creates a consistent glass-style visual treatment across sign-in and sign-up.

## Back Button Handling

The dashboard's back behavior is intentionally simple:

- back switches from a secondary tab to the default tab,
- back on the default tab is handled by the system.

This prevents users from getting stuck inside the bottom-navigation shell.

## Related Files

- `app/src/main/java/com/example/mobiledev/feature/signin/presentation/SignInScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signup/presentation/SignUpScreen.kt`
- `app/src/main/java/com/example/mobiledev/ui/components/AuthScreenContainer.kt`
- `app/src/main/java/com/example/mobiledev/ui/components/AuthInputField.kt`
- `app/src/main/java/com/example/mobiledev/feature/main/presentation/MainScreen.kt`
- `app/src/main/AndroidManifest.xml`

