package com.tdpham.brainyarcade.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: String,
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_games")
data class RecentGame(
    @PrimaryKey val gameId: String,
    val lastPlayed: Long
)

@Entity(tableName = "game_progress")
data class GameProgress(
    @PrimaryKey val gameId: String,
    val currentLevel: Int,
    val maxLevelReached: Int
)
