# Splash / App Launch Experience

## Overview

The app launch experience is implemented through Android splash-screen theming rather than a dedicated Compose screen.
The splash screen is shown before the main UI loads and uses the project branding and color palette.

## Implementation Summary

The splash behavior is controlled by:

- `AndroidManifest.xml`
- `themes.xml`
- `values-v31/themes.xml`
- `splash_window_background.xml`
- `splash_screen_icon.xml`
- `MainActivity.kt`

### Theme-driven splash screen
The launcher activity uses the splash theme:

- `android:theme="@style/Theme.MobileDev.Splash"`

This means the system shows the splash screen automatically at app start.

### Background color
The surrounding splash color is set to:

- `#92DAFF`

This is stored in the shared `splash_background` color resource and referenced by the splash theme on modern Android versions.

### Center icon
The splash icon is centered and drawn using the splash icon drawable.

## Important Notes

- The splash screen is not a Compose destination.
- It is a native Android launch experience based on theme resources.
- The project keeps the same splash styling across supported versions by using theme resources and a drawable background.

## Related Files

- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/values-v31/themes.xml`
- `app/src/main/res/drawable/splash_window_background.xml`
- `app/src/main/res/drawable/splash_screen_icon.xml`
- `app/src/main/java/com/example/mobiledev/MainActivity.kt`

