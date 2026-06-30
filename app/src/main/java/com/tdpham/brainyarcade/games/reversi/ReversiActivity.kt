package com.tdpham.brainyarcade.games.reversi

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class ReversiActivity : BaseGameActivity() {
    override val gameId: String = "reversi"
    private lateinit var gameView: ReversiView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reversi)
        gameView = findViewById(R.id.reversi_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
