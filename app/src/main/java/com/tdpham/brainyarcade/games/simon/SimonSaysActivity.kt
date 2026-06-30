package com.tdpham.brainyarcade.games.simon

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class SimonSaysActivity : BaseGameActivity() {
    override val gameId: String = "simon_says"
    private lateinit var gameView: SimonSaysView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simon_says)
        gameView = findViewById(R.id.simon_view)
        gameView.onGameOver = { score -> onGameOver(score, false) }
        gameView.onWin = { score -> onGameOver(score, true) }
    }
}
