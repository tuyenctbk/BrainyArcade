package com.tdpham.brainyarcade.games.sliding

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class SlidingPuzzleActivity : BaseGameActivity() {
    override val gameId: String = "sliding_puzzle"
    private lateinit var gameView: SlidingPuzzleView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sliding_puzzle)
        gameView = findViewById(R.id.sliding_puzzle_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
