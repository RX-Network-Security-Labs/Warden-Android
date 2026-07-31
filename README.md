# Warden — Real-Time Privacy Monitor for Android

Warden is a lightweight, open-source privacy monitoring app for Android. It watches every app on your device for permission access — camera, microphone, location, and more — and tells you exactly when it happened and whether it was suspicious.

No root. No cloud. No data leaves your device.

---

## Features

**Monitoring**
- Camera, Microphone, Location (fine & coarse), Contacts, SMS & Call Log, Storage, Calendar, Activity Recognition

**Smart Detection**
- Every access is marked as **GOOD**, **BAD**, or **UNKNOWN** based on whether the app was in the foreground or background at the exact moment of access
- Duplicate prevention — same event is never logged twice
- Noise filtering — only real permission operations are logged

**Reporting**
- Export logs in TXT, CSV, or JSON format
- Per-app full permission history
- Quick stats — total logs, suspicious events today

**App**
- Clean Material 3 UI with Dark / Light theme
- Only 1.24 MB
- No internet permission — verified by source
- 100% offline and local storage via Room database
- Low battery impact — optimized polling every 10 seconds

---

## How It Works

```
Any App → uses Camera / Mic / Location / etc.
        ↓
Shizuku Shell → runs "appops get <package>"
        ↓
Warden parses exact access timestamp
        ↓
UsageStats API queried at that exact moment
        ↓
Was the app in foreground at that time?
  YES → GOOD  |  NO → BAD
        ↓
Logged to local Room database
        ↓
Shown in Warden UI instantly
```

| Status | Meaning |
|--------|---------|
| 🟢 GOOD | App was in foreground — normal user-initiated access |
| 🔴 BAD | App was in background — potentially suspicious |
| ⚪ UNKNOWN | Could not determine app state at access time |

---

## Requirements

| Item | Details |
|------|---------|
| Android Version | 11 (API 30) or higher |
| Shizuku | Installed and running |
| Usage Access | Must be granted to Warden |
| Battery | Set to Unrestricted for best results |

---

## Installation

**Step 1 — Set up Shizuku**

Download and set up [Shizuku](https://github.com/RikkaApps/Shizuku). No PC needed — you can activate it using the wireless ADB loopback method directly from your phone.

**Step 2 — Install Warden**

Download the latest APK from [Releases](../../releases/latest), enable "Install from unknown sources" in Android settings, and install.

**Step 3 — Configure**

1. Open Warden → Settings
2. Go to Shizuku Setup and grant permission
3. Enable Live Logging
4. Done — Warden is now monitoring 🛡️

---

## Source Code

Source code is available in the [`Source Code/`](./Source%20Code/) directory, organized by version.

```
Source Code/
└── V1.0.0/
    └── [Full source code of v1.0.0]
```

---

## Privacy & Security

- No internet permission — Warden cannot send data anywhere
- No analytics, no ads, no tracking
- All logs stored locally in a private Room database
- Fully open source under GPL v3

---

## FAQ

**Does Warden need root?**
No. Warden uses Shizuku for shell-level access without root.

**Will it drain my battery?**
Warden polls every 10 seconds using lightweight shell commands. Set battery to Unrestricted for uninterrupted monitoring.

**Why does Camera show as BAD when I used it?**
If you opened and closed the camera quickly (under ~10 seconds), Warden may detect it after it's already closed. Rapid background camera access is genuinely suspicious, so this is by design.

**Does it work on all Android devices?**
Warden works on Android 11+. On OEM devices like Vivo, Xiaomi, and Samsung, Warden automatically falls back to AppOps instead of logcat.

---

## License

GPL v3 — see [LICENSE](./LICENSE) for full text.

---

*Developed and maintained by [RX Network Security Labs](https://github.com/RX-Network-Security-Labs)*
