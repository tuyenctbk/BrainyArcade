package com.tdpham.brainyarcade.games.slitherlink

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class SlitherlinkActivity : BaseGameActivity() {
    override val gameId: String = "slitherlink"
    private lateinit var gameView: SlitherlinkView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slitherlink)
        gameView = findViewById(R.id.slitherlink_view)
        gameView.onWin = { onGameWin() }
    }
}
