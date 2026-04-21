# Current Implementation Overview

## Overview

This page documents the current implemented state of the app shell, auth flow, dashboard tabs, loading behavior, glass styling, and mock request data.

The app is built with Jetpack Compose and MVVM. Most visible screens use a glass-style visual language with translucent cards, subtle borders, and contrast-aware colors so icons and counts stay readable on top of image backgrounds.

## App Shell

The root shell is handled by `MainScreen.kt` and the navigation graph.

Implemented sections:

- Activity tab
- Requests tab
- Account tab

The top app bar uses the app brand image instead of a text title. The right-side hamburger icon is currently present as a placeholder for later menu behavior.

## Authentication Flow

### Sign In

- Users sign in with email or phone plus password.
- The sign-in card is glass-styled and scrollable.
- Submit actions now track loading state.
- The submit button disables while the request is in progress and shows a spinner.

### Sign Up

- Users register with full name, phone number, email, password, and confirm password.
- The sign-up screen uses the same glass auth layout as sign in.
- Submit actions track loading state.
- The submit button disables while the request is in progress and shows a spinner.

### Hospital Sign In

- Hospital admin sign-in already tracks loading and disables submit while processing.
- A circular progress indicator is shown inside the button during submission.

### Session handling

- Successful authentication stores the current principal in `AuthSessionManager`.
- Logout clears the session and returns the user to sign-in.

## Dashboard Tabs

### Activity

- The activity tab shows glass summary cards and smaller stat cards.
- Accent values are contrast-adjusted so green, blue, grey, and other colors stay visible over translucent surfaces.
- The activity tab uses mock summary data from `MockActivityData`.

### Requests

- The requests activity renders a glass analytics summary, filter chips, request cards, dialogs, and error feedback.
- The extra in-screen top app bar was removed so only the main shell app bar remains.
- Requests now show loading in two ways:
  - a centered loader when the list is empty and data is still loading,
  - a linear progress bar when data is already visible and a refresh is in progress.

#### Request content

- Request cards use glass surfaces with contrast-aware status colors.
- Filter chips remain readable against the translucent background.
- Request details and ambulance assignment dialogs continue to use the current request state.

#### Mock request coverage

- Mock request data now contains at least ten items per status filter.
- Statuses covered:
  - Pending
  - Assigned
  - En Route
  - Arrived
  - Completed
  - Cancelled

### Account

- The account tab is now a full glass layout rather than a small summary card.
- It includes:
  - a profile header,
  - session details,
  - security and preference content,
  - quick actions,
  - a logout button.
- The layout is intentionally taller so it fills the screen with useful content.
- Logout is wired to clear the auth session and navigate back to sign-in.

## Glass Styling Rules

Across the app, glass components generally follow this order:

1. translucent surface background
2. subtle light border
3. readable foreground text/icons
4. contrast-adjusted accent colors

This matters especially for blue, grey, cyan, green, and yellow variants, which can disappear on top of low-opacity surfaces if they are not adjusted.

## Loading State Coverage

Loading states are now visible in the main flows:

- Sign In submit loading
- Sign Up submit loading
- Hospital Sign In loading
- Requests list initial loading
- Requests refresh loading
- Staff management loading where applicable
- Hospital dashboard loading where applicable

The goal is to always show the user that work is happening, either with a spinner, a button spinner, or a progress bar.

## Related Files

- `app/src/main/java/com/example/mobiledev/feature/main/presentation/MainScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/emergency/EmergencyDashboardScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signin/presentation/SignInScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signin/presentation/SignInViewModel.kt`
- `app/src/main/java/com/example/mobiledev/feature/signup/presentation/SignUpScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/signup/presentation/SignUpViewModel.kt`
- `app/src/main/java/com/example/mobiledev/feature/hospital/presentation/HospitalSignInScreen.kt`
- `app/src/main/java/com/example/mobiledev/feature/hospital/presentation/HospitalSignInViewModel.kt`
- `app/src/main/java/com/example/mobiledev/navigation/NavGraph.kt`
- `app/src/main/java/com/example/mobiledev/data/mock/MockEmergencyDashboardData.kt`
