package com.tdpham.brainyarcade.games.chess

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class ChessActivity : BaseGameActivity() {
    override val gameId: String = "chess"
    private lateinit var gameView: ChessTacticsView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess)
        gameView = findViewById(R.id.chess_view)
        gameView.onWin = { score -> onGameOver(score, true) }
        gameView.onLose = { onGameOver(0, false) }
    }
}
