package com.tdpham.brainyarcade.games.akari

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class AkariView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 6
    private val grid = Array(size) { IntArray(size) { 0 } }
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
        grid[1][1] = 2 // Wall
        grid[3][3] = 4 // Wall with '1' (1+3)
        grid[4][4] = 2 // Wall
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (grid[i][j] == 1) grid[i][j] = 0
            }
        }
        gameOver = false
        invalidate()
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

                paint.color = when {
                    grid[i][j] >= 2 -> Color.BLACK
                    isIlluminated(i, j) -> Color.YELLOW
                    else -> Color.WHITE
                }
                paint.style = Paint.Style.FILL
                canvas.drawRect(left, top, right, bottom, paint)

                paint.color = Color.DKGRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRect(left, top, right, bottom, paint)

                if (grid[i][j] == 1) {
                    paint.color = Color.parseColor("#FFA500") // Orange
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize * 0.3f, paint)
                }

                if (grid[i][j] >= 3) {
                    paint.color = Color.WHITE
                    paint.textSize = cellSize * 0.5f
                    paint.textAlign = Paint.Align.CENTER
                    val textOffset = (paint.descent() + paint.ascent()) / 2
                    canvas.drawText((grid[i][j] - 3).toString(), left + cellSize / 2, top + cellSize / 2 - textOffset, paint)
                }

                if (i == selectedX && j == selectedY && isFocused && !gameOver) {
                    paint.color = Color.parseColor("#00BCD4")
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 5f
                    canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, paint)
                }
            }
        }
    }

    private fun isIlluminated(x: Int, y: Int): Boolean {
        if (grid[x][y] == 1) return true
        val dirs = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for (d in dirs) {
            var cx = x + d.first; var cy = y + d.second
            while (cx in 0 until size && cy in 0 until size && grid[cx][cy] < 2) {
                if (grid[cx][cy] == 1) return true
                cx += d.first; cy += d.second
            }
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> selectedY = (selectedY - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> toggleBulb()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun toggleBulb() {
        if (grid[selectedX][selectedY] <= 1) {
            grid[selectedX][selectedY] = 1 - grid[selectedX][selectedY]
            checkWin()
        }
    }

    private fun checkWin() {
        // Simplified check
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
