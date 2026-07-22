package com.tdpham.brainyarcade.games.starbattle

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class StarBattleActivity : BaseGameActivity() {
    override val gameId: String = "starbattle"
    private lateinit var gameView: StarBattleView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starbattle)
        gameView = findViewById(R.id.starbattle_view)
        gameView.onWin = { onGameWin() }
    }
}
