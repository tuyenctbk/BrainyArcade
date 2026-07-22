package com.tdpham.brainyarcade.games.bridges

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class BridgesActivity : BaseGameActivity() {
    override val gameId: String = "bridges"
    private lateinit var gameView: BridgesView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bridges)
        gameView = findViewById(R.id.bridges_view)
        gameView.onWin = { onGameWin() }
    }
}
