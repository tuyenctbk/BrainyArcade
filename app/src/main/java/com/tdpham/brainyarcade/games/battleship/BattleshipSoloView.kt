package com.tdpham.brainyarcade.games.battleship

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

class BattleshipSoloView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 6
    private val board = Array(size) { IntArray(size) { 0 } }
    private val target = Array(size) { IntArray(size) { 0 } }
    
    private val rowCounts = IntArray(size)
    private val colCounts = IntArray(size)

    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    
    private var markScale = 1.0f
    private var lastMarkX = -1; private var lastMarkY = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) kotlin.random.Random.Default else kotlin.random.Random(seed)
        for (i in 0 until size) for (j in 0 until size) {
            board[i][j] = 0
            target[i][j] = 0
        }
        // Place random ships
        repeat(5) {
            val rx = rand.nextInt(size); val ry = rand.nextInt(size)
            target[rx][ry] = 1
        }
        
        calculateClues()
        gameOver = false
        invalidate()
    }

    private fun calculateClues() {
        for (i in 0 until size) {
            var rCount = 0; var cCount = 0
            for (j in 0 until size) {
                if (target[j][i] == 1) rCount++
                if (target[i][j] == 1) cCount++
            }
            rowCounts[i] = rCount; colCounts[i] = cCount
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val clueAreaSize = availableSize * 0.2f
        val cellSize = (availableSize - clueAreaSize) / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        // Grid and board
        for (i in 0 until size) {
            for (j in 0 until size) {
                val left = offsetX + i * cellSize; val top = offsetY + j * cellSize
                val right = left + cellSize; val bottom = top + cellSize

                paint.style = Paint.Style.STROKE; paint.color = Color.parseColor("#333333"); paint.strokeWidth = 2f
                canvas.drawRect(left, top, right, bottom, paint)

                val scale = if (i == lastMarkX && j == lastMarkY) markScale else 1.0f
                paint.style = Paint.Style.FILL
                if (board[i][j] == 1) { // Ship
                    paint.color = Color.parseColor("#AAAAAA")
                    val inset = cellSize * 0.1f * (2.0f - scale)
                    canvas.drawRoundRect(left + inset, top + inset, right - inset, bottom - inset, 12f, 12f, paint)
                } else if (board[i][j] == 2) { // Water
                    paint.color = Color.parseColor("#1E3A8A")
                    canvas.drawCircle(left + cellSize/2, top + cellSize/2, cellSize * 0.2f * scale, paint)
                }

                if (i == selectedX && j == selectedY && isFocused) {
                    paint.style = Paint.Style.STROKE; paint.color = neonCyan; paint.strokeWidth = 4f
                    canvas.drawRect(left + 2, top + 2, right - 2, bottom - 2, paint)
                }
            }
        }

        // Clues
        paint.color = Color.WHITE; paint.textSize = cellSize * 0.4f; paint.textAlign = Paint.Align.CENTER
        val textOffset = (paint.descent() + paint.ascent()) / 2
        for (i in 0 until size) {
            canvas.drawText(String.format(java.util.Locale.US, "%d", rowCounts[i]), offsetX + size * cellSize + cellSize/2, offsetY + i * cellSize + cellSize/2 - textOffset, paint)
            canvas.drawText(String.format(java.util.Locale.US, "%d", colCounts[i]), offsetX + i * cellSize + cellSize/2, offsetY + size * cellSize + cellSize/2 - textOffset, paint)
        }

        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f
            canvas.drawText("FLEET LOCATED!", width / 2f, height / 2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { if (selectedY == 0) return false; selectedY = (selectedY - 1 + size) % size }
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
        if (board[selectedX][selectedY] != 0) animateMark(selectedX, selectedY)
        checkWin()
    }

    private fun animateMark(x: Int, y: Int) {
        lastMarkX = x; lastMarkY = y
        val animator = android.animation.ValueAnimator.ofFloat(0.5f, 1.2f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { markScale = it.animatedValue as Float; invalidate() }
        animator.start()
    }

    private fun checkWin() {
        for (i in 0 until size) for (j in 0 until size) {
            if (target[i][j] == 1 && board[i][j] != 1) return
            if (target[i][j] == 0 && board[i][j] == 1) return
        }
        gameOver = true; onWin?.invoke(1000)
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
