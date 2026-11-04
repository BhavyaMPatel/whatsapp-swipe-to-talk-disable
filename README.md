# TouchGuard

TouchGuard is a lightweight Android accessibility service designed to prevent accidental WhatsApp group voice chat popups and voice note recording locks, without interfering with legitimate 1-on-1 or regular VoIP phone calls.

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-yellow.svg?logo=buy-me-a-coffee)](https://buymeacoffee.com/bhavyampatel)

---

## Features

- **Accidental Group Voice Chat Defusal:** Instantly exits accidental "Swipe to talk" group voice rooms while inside the chat interface.
- **Normal Call Protection:** Automatically detects ongoing VoIP calls (speakerphone, mute, screen sharing controls) to ensure real phone calls remain completely uninterrupted.
- **Voice Note Recording Guard:** Cancels locked background voice note recordings triggered by accidental swipes.
- **Ultra-lightweight:** Operates with zero foreground notification bloat, no overlay windows, and instant native view querying (`notificationTimeout="0"`).

## How It Works

TouchGuard uses Android's native `AccessibilityService` API to listen for WhatsApp UI state changes. When an active voice chat banner appears (`"Leave call"`) alongside chat composer elements (`"Message"`, `"Attach"`, `"Wave all"`), TouchGuard performs a near-instant programmatic dismissal.

## Installation & Setup

1. **Download:** Head to the [Releases](https://github.com/YOUR_USERNAME/touchguard/releases) page and download the latest `TouchGuard-v*.apk`.
2. **Install:** Open the APK on your device (if prompted by Google Play Protect, tap *More details* -> *Install anyway*).
3. **Grant Accessibility Permission:**
    - Go to **Settings** -> **Accessibility** -> **Installed Apps**.
    - Tap **TouchGuard** and toggle it **ON**.
4. **Battery Optimization:**
    - Go to **Settings** -> **Apps** -> **TouchGuard** -> **Battery**.
    - Set battery usage to **Unrestricted** so Android's battery manager doesn't put the service to sleep.

## Building from Source

```bash
# Clone the repository
git clone [https://github.com/YOUR_USERNAME/touchguard.git](https://github.com/YOUR_USERNAME/touchguard.git)
cd touchguard

# Build debug APK
./gradlew assembleDebug

# Install directly to an authorized ADB device
adb install -r -d app/build/outputs/apk/debug/app-debug.apk