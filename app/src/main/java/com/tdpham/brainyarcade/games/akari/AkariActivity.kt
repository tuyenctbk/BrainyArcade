package com.tdpham.brainyarcade.games.akari

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class AkariActivity : BaseGameActivity() {
    override val gameId: String = "akari"
    private lateinit var gameView: AkariView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akari)
        gameView = findViewById(R.id.akari_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
