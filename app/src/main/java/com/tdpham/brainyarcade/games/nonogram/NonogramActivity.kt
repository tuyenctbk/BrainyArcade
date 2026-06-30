package com.tdpham.brainyarcade.games.nonogram

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class NonogramActivity : BaseGameActivity() {
    override val gameId: String = "nonograms"
    private lateinit var gameView: NonogramView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nonogram)
        gameView = findViewById(R.id.nonogram_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
