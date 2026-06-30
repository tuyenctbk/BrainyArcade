package com.tdpham.brainyarcade.db

import androidx.room.*

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore)

    @Query("SELECT * FROM game_scores WHERE gameId = :gameId ORDER BY score DESC LIMIT 10")
    suspend fun getTopScores(gameId: String): List<GameScore>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateRecentGame(recent: RecentGame)

    @Query("SELECT * FROM recent_games ORDER BY lastPlayed DESC")
    suspend fun getRecentGames(): List<RecentGame>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProgress(progress: GameProgress)

    @Query("SELECT * FROM game_progress WHERE gameId = :gameId")
    suspend fun getProgress(gameId: String): GameProgress?
}
