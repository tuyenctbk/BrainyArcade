package com.tdpham.brainyarcade.games.mahjong

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class MahjongActivity : BaseGameActivity() {
    override val gameId: String = "mahjong"
    private lateinit var gameView: MahjongView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mahjong)
        gameView = findViewById(R.id.mahjong_view)
        gameView.onWin = { score -> onGameOver(score, true) }
        gameView.onLose = { onGameOver(0, false) }
    }
}
