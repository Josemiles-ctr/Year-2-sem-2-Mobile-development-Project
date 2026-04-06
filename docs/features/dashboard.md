# Dashboard & Bottom Navigation

## Overview

After authentication, the user is taken to the dashboard area.
The dashboard currently uses a bottom navigation layout with placeholder screens for the core app sections.

## Implemented Tabs

The current bottom navigation tabs are:

- **Activity**
- **Requests**
- **Account**

Each tab is rendered with:

- an icon,
- a label below the icon,
- and a centered placeholder body.

## Layout Behavior

### Background
The dashboard screens use a full-screen background image and a subtle overlay for readability.

### Placeholder content
The visible text is wrapped in elevated cards so it remains readable over the image background.

### Back button behavior
The dashboard handles the Android back button naturally:

- if the user is on a non-default tab, back returns to the default tab,
- if the user is already on the default tab, the system back behavior applies.

## Navigation Structure

The dashboard is registered as the `Main` route in the app navigation graph.
Successful sign-in or sign-up navigates directly to this route.

## Related Files

- `app/src/main/java/com/example/mobiledev/feature/main/presentation/MainScreen.kt`
- `app/src/main/java/com/example/mobiledev/navigation/NavGraph.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/drawable/background.jpg`

