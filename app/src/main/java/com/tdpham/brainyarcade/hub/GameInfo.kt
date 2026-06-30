package com.tdpham.brainyarcade.hub

import java.io.Serializable

enum class GameCategory {
    LOGIC, MATH, SPATIAL, MEMORY, WORDS, STRATEGY
}

enum class GameBenefit {
    LOGICAL_DEDUCTION, PATTERN_RECOGNITION, MENTAL_MATH, SPATIAL_AWARENESS, TRAIN_MEMORY, VOCABULARY
}

enum class ScoreType {
    POINTS, TIME, MOVES
}

enum class ProgressionType {
    LEVELS, INFINITE
}

data class GameInfo(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val iconResId: Int,
    val bannerResId: Int,
    val activityClass: Class<*>,
    val category: GameCategory,
    val benefits: List<GameBenefit>,
    val howToPlayResId: Int,
    val scoreType: ScoreType = ScoreType.TIME,
    val progressionType: ProgressionType = ProgressionType.LEVELS
) : Serializable
