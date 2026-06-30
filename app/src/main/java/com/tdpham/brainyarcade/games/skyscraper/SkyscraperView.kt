package com.tdpham.brainyarcade.games.skyscraper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class SkyscraperView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 4
    private val board = Array(size) { IntArray(size) { 0 } }
    private val topClues = intArrayOf(2, 2, 1, 3)
    private val leftClues = intArrayOf(1, 2, 2, 3)
    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
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
        val cellSize = Math.min(width, height) / (size + 2).toFloat()
        val offsetX = cellSize
        val offsetY = cellSize

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        for (i in 0..size) {
            canvas.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + size * cellSize, paint)
            canvas.drawLine(offsetX, offsetY + i * cellSize, offsetX + size * cellSize, offsetY + i * cellSize, paint)
        }

        paint.style = Paint.Style.FILL
        paint.textSize = cellSize * 0.4f
        paint.textAlign = Paint.Align.CENTER
        val textOffset = (paint.descent() + paint.ascent()) / 2
        for (i in 0 until size) {
            canvas.drawText(topClues[i].toString(), offsetX + i * cellSize + cellSize / 2, offsetY / 2 - textOffset, paint)
            canvas.drawText(leftClues[i].toString(), offsetX / 2, offsetY + i * cellSize + cellSize / 2 - textOffset, paint)
        }

        paint.textSize = cellSize * 0.6f
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] > 0) {
                    paint.color = Color.CYAN
                    canvas.drawText(board[i][j].toString(), offsetX + i * cellSize + cellSize / 2, offsetY + j * cellSize + cellSize / 2 - textOffset, paint)
                }
            }
        }

        if (isFocused && !gameOver) {
            paint.color = Color.parseColor("#00BCD4")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            canvas.drawRect(offsetX + selectedX * cellSize + 2, offsetY + selectedY * cellSize + 2, 
                offsetX + (selectedX + 1) * cellSize - 2, offsetY + (selectedY + 1) * cellSize - 2, paint)
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
            onWin?.invoke(1200)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
