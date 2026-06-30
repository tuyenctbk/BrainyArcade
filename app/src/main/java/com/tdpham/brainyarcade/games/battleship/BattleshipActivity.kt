package com.tdpham.brainyarcade.games.battleship

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class BattleshipActivity : BaseGameActivity() {
    override val gameId: String = "battleship"
    private lateinit var gameView: BattleshipSoloView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battleship)
        gameView = findViewById(R.id.battleship_view)
        gameView.onWin = { score -> onGameOver(score, true) }
        gameView.onLose = { onGameOver(0, false) }
    }
}
