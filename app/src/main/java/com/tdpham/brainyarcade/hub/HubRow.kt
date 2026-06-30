package com.tdpham.brainyarcade.hub

/**
 * Represents a row in the Netflix-style discovery hub.
 */
data class HubRow(
    val title: String,
    val games: List<GameInfo>
)
