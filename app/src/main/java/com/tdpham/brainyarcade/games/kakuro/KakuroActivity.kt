package com.tdpham.brainyarcade.games.kakuro

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class KakuroActivity : BaseGameActivity() {
    override val gameId: String = "kakuro"
    private lateinit var gameView: KakuroView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kakuro)
        gameView = findViewById(R.id.kakuro_view)
        gameView.onWin = { onGameWin() }
    }
}
