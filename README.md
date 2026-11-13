# NovaTube TV

<p align="center">
  <b>Modern, open-source streaming client built for Android TV, Google TV, and Amazon Fire TV.</b>
</p>

---

## 📺 Overview

**NovaTube TV** is a lean, customizable, and ads-free media client engineered specifically for television displays and remote-control navigation (D-pad). Derived as a fork and alternative to SmartTube, NovaTube is designed for high-performance video streaming with full support for 4K 60fps HDR, VP9/AV1 codecs, SponsorBlock, background playback, and multi-account switching.

### Key Capabilities
- **Designed for TV**: Optimized UI for Google TV, Android TV, and Fire OS remotes.
- **Hardware-Accelerated Playback**: Custom ExoPlayer engine with AMZN extensions for smooth 4K 60FPS HDR/AV1 playback.
- **No Tracking & Privacy First**: No Google Play Services dependency required.
- **Content Controls & SponsorBlock**: Integrated skipping for sponsors, intros, and self-promotions.
- **Audio & Video Enhancements**: AFR (Auto Frame Rate matching), volume leveling, and subtitle customizations.

---

## 📦 Project Architecture

```
NovaTube/
├── smarttubetv/              # Main TV application module, activities, and UI
├── common/                   # Shared UI, presenters, playback controls, and utils
├── MediaServiceCore/         # Media service backend, parser, and YouTube API client
├── SharedModules/            # Core utilities, networking, and preference managers
├── leanbackassistant/        # Android TV Home screen channel recommendations & global search
├── exoplayer-amzn-2.10.6/    # Amazon/ExoPlayer video engine with HDR & VP9 extensions
├── chatkit/                  # Live chat support overlay
└── slidableactivity/         # Smooth UI slide-in transitions
```

---

## 🏷️ Application Identifiers & Build Flavors

| Flavor | Package Name (Application ID) | Description |
| :--- | :--- | :--- |
| **Stable (`ststable`)** | `tv.novatube.app` | Main release version for end users |
| **Beta (`stbeta`)** | `tv.novatube.app.beta` | Feature testing and preview builds |
| **F-Droid (`stfdroid`)** | `tv.novatube.app.fdroid` | F-Droid compliant variant |

---

## 🛠️ Building & Releasing

### 1. Requirements
- **JDK**: Java 17 or Java 11
- **Android SDK**: Build Tools `30.0.3` / `34.0.0`, NDK `21.0.6113669`
- **Gradle**: 6.x or compatible wrapper

### 2. Build APKs with Gradle Wrapper

From the project root directory:

#### Release Builds:
```bash
# Build Stable APK (Recommended for Google TV & Fire TV releases)
./gradlew assembleStstableRelease

# Build Beta APK
./gradlew assembleStbetaRelease

# Build Universal APK containing all ABIs (armeabi-v7a, arm64-v8a, x86)
./gradlew assembleStstableRelease -PuniversalApk=true
```

#### Debug Builds:
```bash
./gradlew assembleStstableDebug
```

Outputs will be generated in:
`smarttubetv/build/outputs/apk/ststable/release/`

---

## 🚀 Deployment to TV Devices

### Sideloading via ADB:
```bash
# Connect to Fire TV or Google TV (enable ADB in Developer Options)
adb connect 192.168.1.XXX:5555

# Install the generated APK
adb install -r smarttubetv/build/outputs/apk/ststable/release/smarttubetv-ststable-release.apk
```

### Direct Download:
Host your released APK (e.g. on GitHub Releases) and use the **Downloader by AFTVnews** app (Code / Short URL) on Fire TV or Android TV to install directly.

---

## 🔒 Signing Configuration

To configure your own production signing keys for releases, update `gradle.properties` or configure signing credentials:
```properties
RELEASE_STORE_FILE=/path/to/novatube.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=novatube
RELEASE_KEY_PASSWORD=your_key_password
```

---

## 📄 License
This project is licensed under the terms of the GNU General Public License v3.0.
