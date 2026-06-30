package com.tdpham.brainyarcade.games.game2048

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class Game2048Activity : BaseGameActivity() {
    override val gameId: String = "game_2048"
    private lateinit var gameView: Game2048View

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2048)
        gameView = findViewById(R.id.game_2048_view)
        gameView.onScoreUpdate = { score -> updateScoreDisplay(score) }
        gameView.onWin = { score -> onGameOver(score, true) }
        gameView.onLose = { score -> onGameOver(score, false) }
    }
}
