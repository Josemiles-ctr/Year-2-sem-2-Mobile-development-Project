# Project Setup Guide

This guide explains how to get the **Emergency Ambulance Request System** Android project running on your local machine.

---

## Prerequisites

Make sure you have the following installed before cloning the project:

| Tool | Version | Notes |
|------|---------|-------|
| **Android Studio** | Meerkat (2024.3.1) or later | [Download](https://developer.android.com/studio) |
| **JDK** | 11 or later | Bundled with Android Studio |
| **Android SDK** | API Level 36 (Android 16) | Installed via SDK Manager |
| **Kotlin** | 2.0.21 | Managed automatically by Gradle |
| **Gradle** | 9.2.1 | Managed automatically by the Gradle wrapper |

> **Minimum device / emulator:** Android API 24 (Android 7.0 Nougat) or higher.

---

## 1. Clone the Repository

```bash
git clone <repository-url>
cd MobileDev
```

Replace `<repository-url>` with the actual URL of the GitHub repository.

---

## 2. Open the Project in Android Studio

1. Launch **Android Studio**.
2. Click **File → Open** (or **Open** on the welcome screen).
3. Navigate to the cloned `MobileDev` folder and click **OK**.
4. Wait for Android Studio to finish **indexing** and the **Gradle sync** to complete automatically.

---

## 3. Install the Required Android SDK

If the SDK is not already installed, Android Studio will prompt you. To install it manually:

1. Go to **Tools → SDK Manager**.
2. Under the **SDK Platforms** tab, check **Android API 36**.
3. Under the **SDK Tools** tab, ensure the following are installed:
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools
4. Click **Apply** and let the installation finish.

---

## 4. Sync Gradle

If Gradle did not sync automatically:

1. Click **File → Sync Project with Gradle Files**, or
2. Click the **elephant icon (🐘)** in the toolbar.

The project uses the Gradle wrapper, so no manual Gradle installation is required — it will be downloaded automatically on first sync.

---

## 5. Configure `local.properties`

A `local.properties` file is required at the project root. Android Studio generates this automatically, but if it is missing, create it manually:

```properties
sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

Replace `<YourUsername>` with your Windows username. On macOS/Linux the path would be:

```properties
sdk.dir=/Users/<YourUsername>/Library/Android/sdk
```

> ⚠️ Do **not** commit `local.properties` to version control — it is already listed in `.gitignore`.

---

## 6. Run the App

### On a Physical Device
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect the device via USB.
3. Select your device from the device dropdown in Android Studio.
4. Click the **Run ▶** button (or press `Shift + F10`).

### On an Emulator
1. Go to **Tools → Device Manager**.
2. Click **Create Device** and follow the wizard to create a virtual device running API 24 or higher.
3. Start the emulator, then click the **Run ▶** button.

---

## 7. Build the Project (Optional)

To build the project from the command line without Android Studio:

```bash
# On Windows
gradlew.bat assembleDebug

# On macOS / Linux
./gradlew assembleDebug
```

The generated APK will be located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Project Structure

```
MobileDev/
├── app/
│   ├── src/
│   │   ├── main/           # Application source code & resources
│   │   ├── test/           # Unit tests
│   │   └── androidTest/    # Instrumented (UI) tests
│   └── build.gradle.kts    # App-level Gradle config
├── gradle/
│   ├── libs.versions.toml  # Centralised dependency versions
│   └── wrapper/            # Gradle wrapper files
├── build.gradle.kts        # Project-level Gradle config
├── settings.gradle.kts     # Project settings & module includes
├── gradle.properties       # Gradle JVM & AndroidX settings
└── local.properties        # Local SDK path (not committed)
```

---

## Key Dependencies

| Library | Version |
|---------|---------|
| Jetpack Compose BOM | 2024.09.00 |
| Compose Material 3 | via BOM |
| AndroidX Core KTX | 1.17.0 |
| Lifecycle Runtime KTX | 2.10.0 |
| Activity Compose | 1.12.4 |

All dependency versions are managed centrally in `gradle/libs.versions.toml`.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Gradle sync fails | Check your internet connection; Gradle downloads dependencies on first sync |
| `sdk.dir` not found | Verify the path in `local.properties` matches your SDK installation |
| Build fails with SDK version error | Open SDK Manager and install **Android API 36** |
| Emulator won't start | Ensure Intel HAXM or Android Emulator Hypervisor Driver is installed |
| `local.properties` missing | Create it manually as shown in Step 5 |

---

## Need Help?

Refer to the project [README](../README.md) for an overview of the application features and architecture, or reach out to any team member.
