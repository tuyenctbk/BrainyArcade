package com.tdpham.brainyarcade.games.sudoku

import kotlin.random.Random

/**
 * Generates a valid Sudoku board with a single solution.
 */
class SudokuGenerator(seed: Long = -1) {
    private val size = 9
    private val board = Array(size) { IntArray(size) { 0 } }
    private val random = if (seed == -1L) Random.Default else Random(seed)

    fun generate(difficulty: Int): Array<IntArray> {
        fillDiagonal()
        fillRemaining(0, 3)
        removeDigits(difficulty)
        return board.map { it.copyOf() }.toTypedArray()
    }

    private fun fillDiagonal() {
        for (i in 0 until size step 3) {
            fillBox(i, i)
        }
    }

    private fun fillBox(row: Int, col: Int) {
        var num: Int
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                do {
                    num = random.nextInt(1, 10)
                } while (!unusedInBox(row, col, num))
                board[row + i][col + j] = num
            }
        }
    }

    private fun unusedInBox(rowStart: Int, colStart: Int, num: Int): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[rowStart + i][colStart + j] == num) return false
            }
        }
        return true
    }

    private fun fillRemaining(i: Int, j: Int): Boolean {
        var row = i
        var col = j
        if (col >= size && row < size - 1) {
            row += 1
            col = 0
        }
        if (row >= size && col >= size) return true
        if (row < 3) {
            if (col < 3) col = 3
        } else if (row < size - 3) {
            if (col == (row / 3) * 3) col += 3
        } else {
            if (col == size - 3) {
                row += 1
                col = 0
                if (row >= size) return true
            }
        }

        for (num in 1..9) {
            if (isSafe(row, col, num)) {
                board[row][col] = num
                if (fillRemaining(row, col + 1)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    private fun isSafe(i: Int, j: Int, num: Int): Boolean {
        return (unusedInRow(i, num) &&
                unusedInCol(j, num) &&
                unusedInBox(i - i % 3, j - j % 3, num))
    }

    private fun unusedInRow(i: Int, num: Int): Boolean {
        for (j in 0 until size) {
            if (board[i][j] == num) return false
        }
        return true
    }

    private fun unusedInCol(j: Int, num: Int): Boolean {
        for (i in 0 until size) {
            if (board[i][j] == num) return false
        }
        return true
    }

    private fun removeDigits(count: Int) {
        var c = count
        while (c != 0) {
            val cellId = random.nextInt(0, 81)
            val i = cellId / 9
            val j = cellId % 9
            if (board[i][j] != 0) {
                board[i][j] = 0
                c--
            }
        }
    }
}
