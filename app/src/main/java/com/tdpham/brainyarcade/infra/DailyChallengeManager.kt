package com.tdpham.brainyarcade.infra

import com.tdpham.brainyarcade.hub.GameRegistry
import java.util.Calendar

/**
 * Logic to generate a consistent "Daily Challenge" for all users.
 */
object DailyChallengeManager {
    
    fun getDailySeed(): Long {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        return (year * 1000 + day).toLong()
    }

    fun getDailyGameId(): String {
        val seed = getDailySeed()
        val index = (seed % GameRegistry.ALL_GAMES.size).toInt()
        return GameRegistry.ALL_GAMES[index].id
    }
}
