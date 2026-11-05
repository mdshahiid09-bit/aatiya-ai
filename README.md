# 🌟 AATIYA AI - Real-Time Gemini Live Voice Assistant

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7C3AED)](https://m3.material.io)
[![Gemini](https://img.shields.io/badge/AI-Google%20Gemini%20Live-4285F4?logo=google&logoColor=white)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Created and architected by MD Shahid.**

**AATIYA AI** is a native Android voice assistant powered directly by **Google Gemini Multimodal Live API** (WebSockets) and **Gemini Text Generation API**. It delivers true bi-directional, hands-free conversational audio streaming with instantaneous barge-in interruption, hardware-backed Keystore security, and high-fidelity native audio processing.

---

## 🔮 Core Features

- 🎙️ **Real-Time Gemini Live Voice Streaming:**
  - True bidirectional streaming using Google's `BidiGenerateContent` WebSocket protocol.
  - Low-latency native input capture via Android `AudioRecord` (16,000 Hz, 16-bit Mono PCM).
  - High-fidelity native speech output playback via Android `AudioTrack` (24,000 Hz, 16-bit Mono PCM).
  - **No fake audio**: No Android TTS, no simulated responses, no mock connections.

- ⚡ **Instant Barge-In / Interruption Support:**
  - Real-time voice activity detection (VAD).
  - When the user interrupts the assistant mid-sentence, AATIYA AI instantly cancels output generation, flushes the hardware audio buffers, and transitions immediately back to listening mode.

- 🔮 **Reactive 3D-Styled Animated Orb:**
  - Dynamic visualizer with multi-layered radial gradient aura, rotating particle clusters, and specular glow.
  - Dynamically reflects actual states: **Idle**, **Connecting**, **Listening**, **Thinking**, **Speaking**, and **Error**.
  - Dynamically responds to real RMS audio amplitude from microphone input and incoming Gemini speech.

- 🔒 **Android Keystore Hardware Security:**
  - API keys are encrypted at rest using AES-256-GCM backed by the Android Keystore HSM.
  - Master keys never leave secure hardware.
  - No plain-text secrets in SharedPreferences, logs, or network headers.
  - Native token provisioning layer for ephemeral authentication.

- 🌐 **Trilingual Fluency:**
  - Comprehends and responds fluently in **Hindi**, **Hinglish**, and **English**.
  - Customizable system persona instruction.

- 🎛️ **Prebuilt Gemini Live Voices:**
  - Initial preferred voice: **Aoede** (Warm, natural & conversational).
  - Also supports **Charon**, **Fenrir**, **Kore**, and **Puck**.

- 🧪 **Live Diagnostics & Dual Connection Testing:**
  - **Test Text Connection:** Verifies Gemini REST API connectivity, measuring exact response latency.
  - **Test Live Connection:** Opens a real WebSocket connection to Gemini Live, completes the setup handshake, and measures setup latency.
  - Model discovery: Fetches live verified models directly from Google's endpoint (`/v1beta/models`).

---

## 🏗️ Architecture & Technology Stack

| Component | Technology | Description |
|---|---|---|
| **OS Support** | Android 8.0+ (Min SDK 26, Target SDK 35) | High compatibility across modern Android devices |
| **Language** | Kotlin 2.0.21 | Modern idiomatic Kotlin with Coroutines & StateFlow |
| **UI Toolkit** | Jetpack Compose & Material 3 | Declarative dark-themed UI with fluid animations |
| **Architecture** | Clean Architecture + MVVM | Strict separation of UI, Domain, and Data layers |
| **Networking** | OkHttp 4.12.0 | High-performance HTTP client & WebSocket engine |
| **Microphone Input** | `android.media.AudioRecord` | 16kHz 16-bit Linear PCM Mono streaming |
| **Audio Playback** | `android.media.AudioTrack` | 24kHz 16-bit Linear PCM Mono low-latency playback |
| **Security** | Android Keystore | Hardware-backed AES-256-GCM encryption |
| **DI** | Lightweight manual `AppContainer` | Zero-reflection dependency injection |

---

## 📱 Application Screens

1. **Splash Screen:**
   - Premium AATIYA AI animated logo with particle aura.
   - Automatically detects API configuration status and transitions smoothly.

2. **API Setup Screen:**
   - Gemini API Key entry with show/hide toggle.
   - Text Model and Live Voice Model selectors with manual override.
   - Live REST & WebSocket connection test buttons with latency readouts.
   - Real-time verified model list fetcher.

3. **Main Assistant Screen:**
   - Central interactive glowing orb visualizer.
   - Real-time status indicator badge (**Ready**, **Connecting**, **Listening**, **Thinking**, **Speaking**, **Error**).
   - Live caption transcript card.
   - Tactile microphone toggle button with pulsing active ring.
   - Bottom text bar for typed queries.
   - Instant access to Diagnostics Sheet and Settings.

4. **Settings Screen:**
   - API key management (update, replace, delete).
   - Text & Live Voice model configuration.
   - Voice persona selector (**Aoede**, **Charon**, **Fenrir**, **Kore**, **Puck**).
   - Personality / Language prompt editor.
   - Audio pipeline diagnostics & connection latency benchmarks.

---

## 📲 How to Download & Install the APK

The application is built automatically by GitHub Actions on every push and release.

### Method 1: Download from GitHub Actions (Artifact)
1. Open the repository on GitHub: [`mdshahiid09-bit/aatiya-ai`](https://github.com/mdshahiid09-bit/aatiya-ai)
2. Tap the **Actions** tab at the top.
3. Tap the latest workflow run (e.g. **Build AATIYA AI APK**).
4. Scroll down to the **Artifacts** section at the bottom.
5. Tap **`AATIYA-AI-APK`** to download the zip file containing the debug APK.
6. Extract the zip file and tap the APK to install on your Android device.

### Method 2: Download from GitHub Releases
1. Navigate to the **Releases** section on the repository page.
2. Tap the latest release (e.g. `v1.0.x`).
3. Tap the `.apk` asset under **Assets** to download directly.
4. Open the downloaded file to install. If prompted by Android, grant **Install unknown apps** permission for your browser or file manager.

---

## ⚙️ Initial Setup & Testing Guide

Once the APK is installed on your phone:

1. **Launch AATIYA AI.**
2. On the Setup Screen, paste your **Google Gemini API Key** (obtainable free from [Google AI Studio](https://aistudio.google.com/)).
3. (Optional) Select or enter your preferred Text and Live Voice models:
   - Default Text Model: `gemini-2.0-flash`
   - Default Live Voice Model: `gemini-2.0-flash-exp`
4. Tap **Test Text Connection** — you will see a real response returned with round-trip latency in milliseconds.
5. Tap **Test Live Voice Connection** — AATIYA AI will open the WebSocket, execute the setup handshake, verify confirmation, and report connection latency.
6. Tap **Save Settings & Launch Assistant**.
7. Grant the **Microphone** permission when prompted.
8. Tap the central microphone button to start a real-time, low-latency voice conversation!

---

## ⚠️ Important Notes & Quotas

- **Real API Calls:** All interactions go directly to Google's official Gemini API endpoints.
- **API Quotas:** Gemini API usage is subject to the quotas, rate limits, and billing terms of your Google AI Studio account. If you encounter a `429 Rate Limit` or `403 Quota` error, wait a few moments or review your quota tier in Google Cloud Console / AI Studio.
- **Phase 1 Scope:** This release focuses strictly on perfecting real-time bidirectional Gemini Live voice communication, text AI generation, and hardware audio pipelines. Device automation, accessibility services, and background wake words are planned for subsequent phases.

---

## 👤 Author & Credits

- **Developer & Owner:** [MD Shahid](https://github.com/mdshahiid09-bit)
- **Repository:** [`https://github.com/mdshahiid09-bit/aatiya-ai`](https://github.com/mdshahiid09-bit/aatiya-ai)
