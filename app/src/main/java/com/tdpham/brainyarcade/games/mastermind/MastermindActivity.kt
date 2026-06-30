package com.tdpham.brainyarcade.games.mastermind

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class MastermindActivity : BaseGameActivity() {
    override val gameId: String = "mastermind"
    private lateinit var gameView: MastermindView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mastermind)
        gameView = findViewById(R.id.mastermind_view)
        gameView.onWin = { score -> onGameOver(score, true) }
        gameView.onLose = { onGameOver(0, false) }
    }
}
