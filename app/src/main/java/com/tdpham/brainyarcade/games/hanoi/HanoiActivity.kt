package com.tdpham.brainyarcade.games.hanoi

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class HanoiActivity : BaseGameActivity() {
    override val gameId: String = "hanoi"
    private lateinit var gameView: HanoiView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hanoi)
        gameView = findViewById(R.id.hanoi_view)
        gameView.onWin = { moves -> onGameWin(moves) }
        gameView.onMove = { moves -> updateScoreDisplay(moves) }
    }
}
