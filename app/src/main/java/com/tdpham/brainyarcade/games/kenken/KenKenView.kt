package com.tdpham.brainyarcade.games.kenken

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class KenKenView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 4
    private val board = Array(size) { IntArray(size) { 0 } }
    data class Cage(val cells: List<Pair<Int, Int>>, val target: Int, val op: String)
    private val cages = mutableListOf<Cage>()
    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setupLevel()
    }

    private fun setupLevel() {
        cages.add(Cage(listOf(0 to 0, 0 to 1), 5, "+"))
        cages.add(Cage(listOf(1 to 0, 2 to 0), 2, "/"))
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) board[i][j] = 0
        }
        gameOver = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = Math.min(width, height) / size.toFloat()
        val offsetX = (width - cellSize * size) / 2
        val offsetY = (height - cellSize * size) / 2

        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        for (i in 0..size) {
            canvas.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + size * cellSize, paint)
            canvas.drawLine(offsetX, offsetY + i * cellSize, offsetX + size * cellSize, offsetY + i * cellSize, paint)
        }

        paint.strokeWidth = 6f
        paint.textSize = cellSize * 0.25f
        for (cage in cages) {
            val first = cage.cells.first()
            canvas.drawText("${cage.target}${cage.op}", offsetX + first.first * cellSize + 10, offsetY + first.second * cellSize + paint.textSize + 5, paint)
        }

        paint.textAlign = Paint.Align.CENTER
        val textOffset = (paint.descent() + paint.ascent()) / 2
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] > 0) {
                    paint.color = Color.CYAN
                    canvas.drawText(String.format(java.util.Locale.US, "%d", board[i][j]), offsetX + i * cellSize + cellSize / 2, offsetY + j * cellSize + cellSize / 2 - textOffset, paint)
                }
            }
        }

        if (isFocused && !gameOver) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#00BCD4")
            paint.strokeWidth = 5f
            canvas.drawRect(offsetX + selectedX * cellSize + 2, offsetY + selectedY * cellSize + 2, 
                offsetX + (selectedX + 1) * cellSize - 2, offsetY + (selectedY + 1) * cellSize - 2, paint)
            paint.style = Paint.Style.FILL
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> selectedY = (selectedY - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_4 -> {
                board[selectedX][selectedY] = keyCode - KeyEvent.KEYCODE_0
                checkWin()
            }
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_0 -> board[selectedX][selectedY] = 0
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun checkWin() {
        if (board.all { row -> row.all { it != 0 } }) {
            gameOver = true
            onWin?.invoke(1000)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
