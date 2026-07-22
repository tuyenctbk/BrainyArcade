package com.tdpham.brainyarcade.infra

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages onboarding state and rating suggestions.
 */
class OnboardingManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("brainy_prefs", Context.MODE_PRIVATE)

    fun isFirstRun(): Boolean {
        val firstRun = prefs.getBoolean("first_run", true)
        if (firstRun) {
            prefs.edit().putBoolean("first_run", false).apply()
        }
        return firstRun
    }

    fun incrementGamesWon() {
        val count = prefs.getInt("games_won", 0) + 1
        prefs.edit().putInt("games_won", count).apply()
    }

    fun getGamesWonCount(): Int {
        return prefs.getInt("games_won", 0)
    }

    fun incrementLaunchCount() {
        val launchCount = prefs.getInt("launch_count", 0) + 1
        prefs.edit().putInt("launch_count", launchCount).apply()
    }

    fun shouldShowRating(): Boolean {
        val launchCount = prefs.getInt("launch_count", 0)
        val alreadyRated = prefs.getBoolean("already_rated", false)
        val gamesWon = prefs.getInt("games_won", 0)
        val lastPrompted = prefs.getLong("last_prompted_time", 0L)
        val now = System.currentTimeMillis()
        val cooldownMs = 3 * 24 * 60 * 60 * 1000L // 3 days

        val shouldPrompt = !alreadyRated && 
                           launchCount >= 3 && 
                           gamesWon >= 2 && 
                           (now - lastPrompted > cooldownMs)

        if (shouldPrompt) {
            prefs.edit().putLong("last_prompted_time", now).apply()
        }
        return shouldPrompt
    }

    fun markRated() {
        prefs.edit().putBoolean("already_rated", true).apply()
    }

    fun shouldShowGameInstructions(gameId: String): Boolean {
        val key = "instr_seen_$gameId"
        val seen = prefs.getBoolean(key, false)
        if (!seen) {
            prefs.edit().putBoolean(key, true).apply()
        }
        return !seen
    }
}
