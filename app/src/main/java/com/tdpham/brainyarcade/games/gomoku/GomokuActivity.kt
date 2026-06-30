package com.tdpham.brainyarcade.games.gomoku

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class GomokuActivity : BaseGameActivity() {
    override val gameId: String = "gomoku"
    private lateinit var gameView: GomokuView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gomoku)
        gameView = findViewById(R.id.gomoku_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
