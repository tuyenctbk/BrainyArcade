package com.tdpham.brainyarcade.games.kenken

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class KenKenActivity : BaseGameActivity() {
    override val gameId: String = "kenken"
    private lateinit var gameView: KenKenView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kenken)
        gameView = findViewById(R.id.kenken_view)
        gameView.onWin = { onGameWin() }
    }
}
