# Remote Config Configuration

This document outlines the Firebase Remote Config parameters used in the BrainyArcade application, as defined in `RemoteConfigHelper.kt`.

## Ad Configuration Parameters

| Remote Config Key | Constant Name | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `ads_min_days` | `KEY_MIN_DAYS` | 7 | Minimum days since first install before showing ads. |
| `ads_min_opens` | `KEY_MIN_OPENS` | 10 | Minimum number of app opens before showing ads. |
| `ads_min_session_seconds` | `KEY_MIN_SESSION_SECONDS` | 10 | Minimum session duration (in seconds) required to show an ad. |
| `ads_cooldown_seconds` | `KEY_COOLDOWN_SECONDS` | 120 | Minimum time (in seconds) between consecutive ads. |
| `ads_max_per_session` | `KEY_MAX_PER_SESSION` | 3 | Maximum number of ads to be shown in a single session. |
| `latest_version` | `KEY_LATEST_VERSION` | 0 | The latest version code of the app available on the store. |

## Fetching Strategy
- **Minimum Fetch Interval:** 1 hour (3600 seconds).
- **Activation:** `fetchAndActivate()` is called during initialization to ensure the latest values are applied.
