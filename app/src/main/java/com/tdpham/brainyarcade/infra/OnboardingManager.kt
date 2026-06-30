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

    fun shouldShowRating(): Boolean {
        val launchCount = prefs.getInt("launch_count", 0) + 1
        prefs.edit().putInt("launch_count", launchCount).apply()
        
        val alreadyRated = prefs.getBoolean("already_rated", false)
        return !alreadyRated && (launchCount == 5 || launchCount == 15 || launchCount == 30)
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
