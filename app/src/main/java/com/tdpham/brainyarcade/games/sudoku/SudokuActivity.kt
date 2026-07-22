package com.tdpham.brainyarcade.games.sudoku

import android.os.Bundle
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.BaseGameActivity
import com.tdpham.brainyarcade.infra.GameView

class SudokuActivity : BaseGameActivity() {
    override val gameId: String = "sudoku"
    private lateinit var sudokuView: SudokuView

    override fun getGameView(): GameView = sudokuView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)
        sudokuView = findViewById(R.id.sudoku_view)
        sudokuView.onWin = { onGameWin() }
    }
}
