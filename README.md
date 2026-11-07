# TouchGuard

TouchGuard is a lightweight Android accessibility service designed to automatically intercept and exit accidental WhatsApp group voice chat rooms ("Swipe to talk"), without interfering with legitimate 1-on-1 or regular VoIP phone calls.

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-yellow.svg?logo=buy-me-a-coffee)](https://buymeacoffee.com/bhavyampatel)

---

## Features

- **Accidental Group Voice Chat Defusal:** Instantly exits accidental "Swipe to talk" group voice rooms while inside the chat interface.
- **Normal Call Protection:** Automatically identifies VoIP controls (speakerphone, mute, screen sharing, minimize call) to ensure legitimate phone calls remain completely uninterrupted.
- **Context-Aware Gating:** Validates conversation indicators before taking action to prevent false positives in personal chats or settings menus.
- **Ultra-lightweight:** Operates with zero foreground notification bloat, no overlay windows, and instant native view querying (`notificationTimeout="0"`).

## How It Works

TouchGuard uses Android's native `AccessibilityService` API to listen for WhatsApp UI state changes. It applies a two-stage safety gate:

1. **Target Identification:** Searches for the active voice chat exit action (`"Leave call"`).
2. **Context Verification:** Confirms the presence of in-chat conversation elements (`"Message"`, `"Attach"`, `"Wave all"`) while ensuring full-screen VoIP phone call indicators (`"Minimise call"`, `"Turn speaker phone on"`) are absent.

Once confirmed, TouchGuard programmatically dismisses the room within milliseconds.

---

## ⚠️ Locale Support & Limitations

- **Currently English Only:** TouchGuard currently relies on English UI text matching (`"Leave call"`, `"Message"`, etc.).
- **Other Languages (Gujarati, Hindi, etc.):** If WhatsApp is set to another device language, WhatsApp translates the buttons (for example, `"કૉલ છોડો"` in Gujarati). As a result, TouchGuard **will not detect or auto-dismiss voice rooms** on non-English WhatsApp setups.
- **Adding Other Locales:** Community contributions for localized string mappings in `CallDismissalPolicy.kt` are welcome! See the [Contributing](#contributing--issues) section below.

---

## Installation & Setup

1. **Download:** Head to the [Releases](https://github.com/bhavyampatel/touchguard/releases) page and download the latest `TouchGuard-v*.apk`.
2. **Install:** Open the APK on your device (if prompted by Google Play Protect, tap *More details* -> *Install anyway*).
3. **Grant Accessibility Permission:**
   - Go to **Settings** -> **Accessibility** -> **Installed Apps**.
   - Tap **TouchGuard** and toggle it **ON**.
4. **Battery Optimization:**
   - Go to **Settings** -> **Apps** -> **TouchGuard** -> **Battery**.
   - Set battery usage to **Unrestricted** so Android's battery manager doesn't put the service to sleep.

## Contributing & Issues

Want to add support for your language?

Open app/src/main/java/com/example/touchguard/CallDismissalPolicy.kt.

Add your language's translated strings to LEAVE_CALL_LABELS, CALL_SCREEN_INDICATORS, and GROUP_VOICE_CHAT_INDICATORS.

Add a corresponding test in CallDismissalPolicyTest.kt and submit a Pull Request!

Found a bug or have a suggestion? Open an issue using the Bug Report Template.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Building from Source

```bash
# Clone the repository
git clone [https://github.com/bhavyampatel/touchguard.git](https://github.com/bhavyampatel/touchguard.git)
cd touchguard

# Run JVM Unit Tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Install directly to an authorized ADB device
adb install -r -d app/build/outputs/apk/debug/app-debug.apk