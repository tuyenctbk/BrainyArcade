package com.tdpham.brainyarcade.games.minesweeper

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class MinesweeperActivity : BaseGameActivity() {
    override val gameId: String = "minesweeper"
    private lateinit var gameView: MinesweeperView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minesweeper)
        gameView = findViewById(R.id.minesweeper_view)
        gameView.onWin = { onGameWin() }
        gameView.onLose = { onGameOver(0, false) }
    }
}
