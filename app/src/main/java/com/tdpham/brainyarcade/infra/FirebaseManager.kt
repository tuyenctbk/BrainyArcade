package com.tdpham.brainyarcade.infra

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        try {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context)
            Log.d(TAG, "Firebase Analytics initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase SDK not fully configured or JSON missing: ${e.message}")
        }
    }

    fun logGameStart(gameId: String) {
        try {
            val bundle = Bundle().apply {
                putString("game_id", gameId)
                putLong("timestamp", System.currentTimeMillis())
            }
            firebaseAnalytics?.logEvent("game_start", bundle)
            Log.d(TAG, "Logged event: game_start ($gameId)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log event: ${e.message}")
        }
    }

    fun logGameWin(gameId: String, score: Int) {
        try {
            val bundle = Bundle().apply {
                putString("game_id", gameId)
                putInt("score", score)
                putLong("timestamp", System.currentTimeMillis())
            }
            firebaseAnalytics?.logEvent("game_win", bundle)
            Log.d(TAG, "Logged event: game_win ($gameId, score=$score)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log event: ${e.message}")
        }
    }

    fun logNonFatal(message: String) {
        try {
            FirebaseCrashlytics.getInstance().recordException(Exception(message))
            Log.d(TAG, "Recorded non-fatal exception in Crashlytics: $message")
        } catch (e: Exception) {
            Log.w(TAG, "Crashlytics not initialized: ${e.message}")
        }
    }
}
