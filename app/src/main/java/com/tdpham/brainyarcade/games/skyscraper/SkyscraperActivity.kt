package com.tdpham.brainyarcade.games.skyscraper

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class SkyscraperActivity : BaseGameActivity() {
    override val gameId: String = "skyscraper"
    private lateinit var gameView: SkyscraperView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skyscraper)
        gameView = findViewById(R.id.skyscraper_view)
        gameView.onWin = { onGameWin() }
    }
}
