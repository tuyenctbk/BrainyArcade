package com.tdpham.brainyarcade.games.kakuro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class KakuroView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 5
    // -1: blocked, 0: empty, 1-9: value
    private val board = Array(size) { IntArray(size) { 0 } }
    
    private var selectedX = 1
    private var selectedY = 1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setupLevel()
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) board[i][j] = 0
        }
        setupLevel()
        gameOver = false
        invalidate()
    }

    private fun setupLevel() {
        for (i in 0 until size) {
            board[0][i] = -1
            board[i][0] = -1
        }
        board[1][1] = 0; board[1][2] = 0
        board[2][1] = 0; board[2][2] = 0
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = Math.min(width, height) / size.toFloat()
        val offsetX = (width - cellSize * size) / 2
        val offsetY = (height - cellSize * size) / 2

        for (i in 0 until size) {
            for (j in 0 until size) {
                val left = offsetX + i * cellSize
                val top = offsetY + j * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                if (board[i][j] == -1) {
                    paint.color = Color.BLACK
                    canvas.drawRect(left, top, right, bottom, paint)
                    paint.color = Color.WHITE
                    paint.strokeWidth = 2f
                    canvas.drawLine(left, top, right, bottom, paint)
                } else {
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(left, top, right, bottom, paint)
                    paint.style = Paint.Style.FILL

                    if (board[i][j] > 0) {
                        paint.color = Color.CYAN
                        paint.textSize = cellSize * 0.6f
                        paint.textAlign = Paint.Align.CENTER
                        val textOffset = (paint.descent() + paint.ascent()) / 2
                        canvas.drawText(String.format(java.util.Locale.US, "%d", board[i][j]), left + cellSize / 2, top + cellSize / 2 - textOffset, paint)
                    }
                }

                if (i == selectedX && j == selectedY && isFocused) {
                    paint.style = Paint.Style.STROKE
                    paint.color = Color.parseColor("#00BCD4")
                    paint.strokeWidth = 5f
                    canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, paint)
                    paint.style = Paint.Style.FILL
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> move(0, -1)
            KeyEvent.KEYCODE_DPAD_DOWN -> move(0, 1)
            KeyEvent.KEYCODE_DPAD_LEFT -> move(-1, 0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> move(1, 0)
            in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_9 -> {
                board[selectedX][selectedY] = keyCode - KeyEvent.KEYCODE_0
                checkWin()
            }
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_0 -> board[selectedX][selectedY] = 0
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun move(dx: Int, dy: Int) {
        val nx = (selectedX + dx + size) % size
        val ny = (selectedY + dy + size) % size
        if (board[nx][ny] != -1) {
            selectedX = nx
            selectedY = ny
        }
    }

    private fun checkWin() {
        // Simple win condition: no zeros left
        if (board.all { row -> row.all { it != 0 } }) {
            gameOver = true
            onWin?.invoke(800)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
            val cellSize = Math.min(width, height) / size.toFloat()
            val offsetX = (width - cellSize * size) / 2
            val offsetY = (height - cellSize * size) / 2
            
            val x = ((event.x - offsetX) / cellSize).toInt()
            val y = ((event.y - offsetY) / cellSize).toInt()
            
            if (x in 0 until size && y in 0 until size && board[x][y] != -1) {
                selectedX = x
                selectedY = y
                invalidate()
                requestFocus()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
