package com.tdpham.brainyarcade.games.wordle

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity

import com.tdpham.brainyarcade.infra.GameView

class WordleActivity : BaseGameActivity() {
    override val gameId: String = "wordle"
    private lateinit var gameView: WordleView

    override fun getGameView(): GameView = gameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wordle)
        gameView = findViewById(R.id.wordle_view)
        gameView.onWin = { attempts -> onGameOver(attempts, true) }
        gameView.onLose = { onGameOver(maxAttempts, false) }
    }

    private val maxAttempts = 6
}
