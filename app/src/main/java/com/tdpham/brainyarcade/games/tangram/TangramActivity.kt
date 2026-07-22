package com.tdpham.brainyarcade.games.tangram

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class TangramActivity : BaseGameActivity() {
    override val gameId: String = "tangram"
    private lateinit var gameView: TangramView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tangram)
        gameView = findViewById(R.id.tangram_view)
        gameView.onWin = { onGameWin() }
    }
}
