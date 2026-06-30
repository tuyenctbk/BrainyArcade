package com.tdpham.brainyarcade.games.sokoban

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class SokobanActivity : BaseGameActivity() {
    override val gameId: String = "sokoban"
    private lateinit var gameView: SokobanView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sokoban)
        gameView = findViewById(R.id.sokoban_view)
        gameView.onWin = { moves -> onGameWin(moves) }
        gameView.onMove = { moves -> updateScoreDisplay(moves) }
    }
}
