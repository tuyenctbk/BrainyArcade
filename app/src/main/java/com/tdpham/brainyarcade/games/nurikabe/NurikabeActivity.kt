package com.tdpham.brainyarcade.games.nurikabe

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class NurikabeActivity : BaseGameActivity() {
    override val gameId: String = "nurikabe"
    private lateinit var gameView: NurikabeView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nurikabe)
        gameView = findViewById(R.id.nurikabe_view)
        gameView.onWin = { score -> onGameWin(score) }
    }
}
