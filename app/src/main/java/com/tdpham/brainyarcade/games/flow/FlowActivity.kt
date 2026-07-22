package com.tdpham.brainyarcade.games.flow

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class FlowActivity : BaseGameActivity() {
    override val gameId: String = "flow_free"
    private lateinit var gameView: FlowFreeView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow)
        gameView = findViewById(R.id.flow_view)
        gameView.onWin = { onGameWin() }
    }
}
