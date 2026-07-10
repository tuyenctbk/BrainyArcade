# AdMob Monetization Strategy: BrainyArcade

This document summarizes the technical implementation and user experience strategy for AdMob ads within BrainyArcade. The goal is to maximize revenue while maintaining a premium, "Brainy" user experience that respects player retention.

---

## 1. Ad Types & Placement

| Ad Type | Placement | Purpose | Trigger |
| :--- | :--- | :--- | :--- |
| **Banner** | Bottom of Hub (MainActivity) | Baseline Revenue | Constant visibility in the discovery hub. |
| **Interstitial** | Exit Game to Hub | Primary Revenue | User finishes or quits a game (Throttled). |
| **Rewarded** | Level Selection Dialog | Engagement & Utility | User opts-in to unlock a locked level early. |

---

## 2. The "Brainy" Throttling Engine
To prevent ad fatigue, interstitial ads are gated by a multi-layer eligibility system.

### A. Initial Qualification (The Grace Period)
Ads will **only** start appearing once a user meets all three criteria:
*   **Install Age**: >= 3 days (allows user to fall in love with the app first).
*   **App Opens**: >= 10 opens (ensures they are a returning, engaged user).
*   **Session Duration**: >= 10 seconds (prevents ads on accidental/quick launches).

### B. Frequency Capping (The Cooldown)
Even if qualified, ads are limited by:
*   **Ad Cooldown**: Minimum 120 seconds between any two ads.
*   **Session Cap**: Maximum of 3 interstitial ads per app session.
*   **Rewarded-Reset**: Watching a Rewarded Ad manually resets the Interstitial cooldown to prevent back-to-back ads.

---

## 3. Remote Configuration
All monetization limits are controlled via **Firebase Remote Config**, allowing real-time adjustments without a Play Store update.

| Key | Default Value | Description |
| :--- | :--- | :--- |
| `ads_min_days` | 3 | Days to wait after install. |
| `ads_min_opens` | 10 | App opens required. |
| `ads_min_session_seconds` | 10 | Startup delay in current session. |
| `ads_cooldown_seconds` | 120 | Minimum time between ads. |
| `ads_max_per_session` | 3 | Max interstitials per session. |

---

## 4. Technical Best Practices

### Lifecycle Binding
Banner ads in the Hub are bound to the Activity lifecycle to save battery and data:
*   `onResume()` -> `adView.resume()`
*   `onPause()` -> `adView.pause()`
*   `onDestroy()` -> `adView.destroy()`

### Smart Pre-loading
*   `AdsManager` automatically pre-loads the next Interstitial or Rewarded ad immediately after one is dismissed.
*   If an ad is not ready when requested, the app skips it gracefully and logs the event, ensuring no "broken" experience for the user.

### Localized Formatting
All ad-related UI (Toasts, Dialog items) use `Locale.US` for numeric formatting to ensure consistent rendering across all regions.

---

## 5. Production Readiness Checklist
- [ ] Replace `ca-app-pub-3940256099942544/...` test IDs with production Unit IDs in `AdsManager.kt` and `activity_main.xml`.
- [ ] Add the 5 Remote Config keys to the Firebase Console.
- [ ] Verify `google-services.json` is correctly linked to the production Firebase project.
- [ ] Logcat debug tag: `AdsManager`.
