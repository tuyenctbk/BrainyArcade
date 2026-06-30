package com.tdpham.brainyarcade.games.nurikabe

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class NurikabeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 5
    // 0: unknown (white), 1: water (shaded), 2: island (white with dot or circle)
    private val state = Array(size) { IntArray(size) { 0 } }
    private val clues = Array(size) { IntArray(size) { -1 } }
    
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
        clues[0][0] = 3
        clues[2][2] = 1
        clues[4][4] = 2
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) state[i][j] = 0
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

                paint.color = when (state[i][j]) {
                    1 -> Color.BLACK
                    else -> Color.WHITE
                }
                paint.style = Paint.Style.FILL
                canvas.drawRect(left, top, right, bottom, paint)

                paint.color = Color.DKGRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawRect(left, top, right, bottom, paint)

                if (state[i][j] == 2) {
                    paint.color = Color.LTGRAY
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize * 0.1f, paint)
                }

                if (clues[i][j] != -1) {
                    paint.color = Color.BLACK
                    paint.style = Paint.Style.FILL
                    paint.textSize = cellSize * 0.5f
                    paint.textAlign = Paint.Align.CENTER
                    val textOffset = (paint.descent() + paint.ascent()) / 2
                    canvas.drawText(clues[i][j].toString(), left + cellSize / 2, top + cellSize / 2 - textOffset, paint)
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> selectedY = (selectedY - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> toggle(1)
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_C -> toggle(2)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun toggle(type: Int) {
        if (clues[selectedX][selectedY] != -1) return
        state[selectedX][selectedY] = if (state[selectedX][selectedY] == type) 0 else type
        checkWin()
    }

    private fun checkWin() {
        // Simplified check
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
