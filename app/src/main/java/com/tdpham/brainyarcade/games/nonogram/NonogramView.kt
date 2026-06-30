package com.tdpham.brainyarcade.games.nonogram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.GameView

class NonogramView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 5
    private val board = Array(size) { IntArray(size) { 0 } }
    private val target = Array(size) { IntArray(size) { 0 } }
    private val rowClues = Array(size) { mutableListOf<Int>() }
    private val colClues = Array(size) { mutableListOf<Int>() }
    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private var cellScale = 1.0f
    private var lastToggledX = -1
    private var lastToggledY = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) kotlin.random.Random.Default else kotlin.random.Random(seed)
        for (i in 0 until size) {
            for (j in 0 until size) {
                target[i][j] = if (rand.nextFloat() > 0.4) 1 else 0
                board[i][j] = 0
            }
        }
        calculateClues()
        gameOver = false
        invalidate()
    }

    private fun calculateClues() {
        for (i in 0 until size) { rowClues[i].clear(); colClues[i].clear() }
        for (j in 0 until size) {
            var count = 0
            for (i in 0 until size) if (target[i][j] == 1) count++ else if (count > 0) { rowClues[j].add(count); count = 0 }
            if (count > 0) rowClues[j].add(count)
        }
        for (i in 0 until size) {
            var count = 0
            for (j in 0 until size) if (target[i][j] == 1) count++ else if (count > 0) { colClues[i].add(count); count = 0 }
            if (count > 0) colClues[i].add(count)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val clueAreaSize = 100f
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = (availableSize - clueAreaSize) / size
        val offsetX = (width - availableSize) / 2 + clueAreaSize
        val offsetY = (height - availableSize) / 2 + clueAreaSize

        paint.textSize = cellSize * 0.4f
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.WHITE
        for (j in 0 until size) {
            val clueStr = rowClues[j].joinToString(" ")
            canvas.drawText(clueStr, offsetX - clueAreaSize / 2, offsetY + j * cellSize + cellSize / 2 + paint.textSize / 3, paint)
        }
        for (i in 0 until size) {
            var yOffset = 0f
            for (c in colClues[i]) {
                canvas.drawText(c.toString(), offsetX + i * cellSize + cellSize / 2, offsetY - clueAreaSize + 30 + yOffset, paint)
                yOffset += paint.textSize
            }
        }

        for (i in 0 until size) {
            for (j in 0 until size) {
                val left = offsetX + i * cellSize; val top = offsetY + j * cellSize
                val right = left + cellSize; val bottom = top + cellSize

                paint.style = Paint.Style.STROKE; paint.color = Color.DKGRAY; paint.strokeWidth = 2f
                canvas.drawRect(left, top, right, bottom, paint)

                paint.style = Paint.Style.FILL
                if (board[i][j] == 1) {
                    val scale = if (i == lastToggledX && j == lastToggledY) cellScale else 1.0f
                    paint.color = neonCyan
                    val margin = 6f + (cellSize * (1f - scale))
                    canvas.drawRect(left + margin, top + margin, right - margin, bottom - margin, paint)
                } else if (board[i][j] == 2) {
                    paint.color = Color.RED; paint.strokeWidth = 4f
                    canvas.drawLine(left + 15, top + 15, right - 15, bottom - 15, paint)
                    canvas.drawLine(right - 15, top + 15, left + 15, bottom - 15, paint)
                }

                if (i == selectedX && j == selectedY && isFocused) {
                    paint.style = Paint.Style.STROKE; paint.color = Color.WHITE; paint.strokeWidth = 6f
                    canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, paint)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (selectedY == 0) return false
                selectedY = (selectedY - 1 + size) % size
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> toggle(1)
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_X -> toggle(2)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun toggle(type: Int) {
        board[selectedX][selectedY] = if (board[selectedX][selectedY] == type) 0 else type
        if (board[selectedX][selectedY] == 1) animateCellPop(selectedX, selectedY)
        checkWin()
    }

    private fun animateCellPop(x: Int, y: Int) {
        lastToggledX = x
        lastToggledY = y
        val animator = android.animation.ValueAnimator.ofFloat(0.8f, 1.1f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { 
            cellScale = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun checkWin() {
        for (i in 0 until size) for (j in 0 until size) {
            if ((board[i][j] == 1) != (target[i][j] == 1)) return
        }
        gameOver = true; onWin?.invoke(1000)
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
