package com.tdpham.brainyarcade.infra

import android.content.Context
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfigHelper {
    private const val TAG = "RemoteConfigHelper"

    private const val KEY_MIN_DAYS = "ads_min_days"
    private const val KEY_MIN_OPENS = "ads_min_opens"
    private const val KEY_MIN_SESSION_SECONDS = "ads_min_session_seconds"
    private const val KEY_COOLDOWN_SECONDS = "ads_cooldown_seconds"
    private const val KEY_MAX_PER_SESSION = "ads_max_per_session"
    private const val KEY_LATEST_VERSION = "latest_version"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    fun initialize(context: Context) {
        try {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // Fetch hourly
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)

            val defaultMap = mapOf(
                KEY_MIN_DAYS to 7L,
                KEY_MIN_OPENS to 20L,
                KEY_MIN_SESSION_SECONDS to 30L,
                KEY_COOLDOWN_SECONDS to 180L,
                KEY_MAX_PER_SESSION to 3L,
                KEY_LATEST_VERSION to 0L
            )
            remoteConfig.setDefaultsAsync(defaultMap)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Remote Config fetched and activated successfully.")
                    } else {
                        Log.w(TAG, "Remote Config fetch failed.")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Remote Config initialization failed: ${e.message}")
        }
    }

    fun getMinDays(): Int {
        return remoteConfig.getLong(KEY_MIN_DAYS).toInt()
    }

    fun getMinOpens(): Int {
        return remoteConfig.getLong(KEY_MIN_OPENS).toInt()
    }

    fun getMinSessionSeconds(): Int {
        return remoteConfig.getLong(KEY_MIN_SESSION_SECONDS).toInt()
    }

    fun getCooldownSeconds(): Int {
        return remoteConfig.getLong(KEY_COOLDOWN_SECONDS).toInt()
    }

    fun getMaxPerSession(): Int {
        return remoteConfig.getLong(KEY_MAX_PER_SESSION).toInt()
    }

    fun getLatestVersion(): Int {
        return remoteConfig.getLong(KEY_LATEST_VERSION).toInt()
    }
}
