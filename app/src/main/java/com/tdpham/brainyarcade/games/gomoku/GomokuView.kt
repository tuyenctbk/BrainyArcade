package com.tdpham.brainyarcade.games.gomoku

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

class GomokuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 15
    private val board = Array(size) { IntArray(size) { 0 } } 
    private var currentPlayer = 2 
    private var selectedX = 7
    private var selectedY = 7
    private var gameOver = false
    private var winner = 0
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    
    private var lastPlacedX = -1; private var lastPlacedY = -1
    private var stoneScale = 1.0f

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) for (j in 0 until size) board[i][j] = 0
        currentPlayer = 2
        gameOver = false
        winner = 0
        lastPlacedX = -1; lastPlacedY = -1
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        // Board background
        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 8f, 8f, paint)

        // Grid lines
        paint.color = Color.parseColor("#444444")
        paint.strokeWidth = 1f
        for (i in 0 until size) {
            val mid = cellSize / 2
            canvas.drawLine(offsetX + i * cellSize + mid, offsetY + mid, offsetX + i * cellSize + mid, offsetY + availableSize - mid, paint)
            canvas.drawLine(offsetX + mid, offsetY + i * cellSize + mid, offsetX + availableSize - mid, offsetY + i * cellSize + mid, paint)
        }

        // Selection
        if (isFocused && !gameOver) {
            paint.color = neonCyan
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 4f
            canvas.drawRect(offsetX + selectedX * cellSize + 4, offsetY + selectedY * cellSize + 4, 
                offsetX + (selectedX + 1) * cellSize - 4, offsetY + (selectedY + 1) * cellSize - 4, paint)
        }

        // Stones
        paint.style = Paint.Style.FILL
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] != 0) {
                    val scale = if (i == lastPlacedX && j == lastPlacedY) stoneScale else 1.0f
                    paint.color = if (board[i][j] == 1) Color.WHITE else Color.BLACK
                    canvas.drawCircle(offsetX + i * cellSize + cellSize / 2, 
                        offsetY + j * cellSize + cellSize / 2, cellSize * 0.4f * scale, paint)
                    
                    if (board[i][j] == 2) { // Add subtle outline for black stones on dark background
                        paint.style = Paint.Style.STROKE; paint.color = Color.parseColor("#333333")
                        paint.strokeWidth = 1f
                        canvas.drawCircle(offsetX + i * cellSize + cellSize / 2, 
                            offsetY + j * cellSize + cellSize / 2, cellSize * 0.4f * scale, paint)
                        paint.style = Paint.Style.FILL
                    }
                }
            }
        }

        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f; paint.textAlign = Paint.Align.CENTER
            val winMsg = if (winner == 1) "WHITE WINS!" else "BLACK WINS!"
            canvas.drawText(winMsg, width / 2f, height / 2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { if (selectedY == 0) return false; selectedY = (selectedY - 1 + size) % size }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> placeStone()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun placeStone() {
        if (board[selectedX][selectedY] == 0) {
            board[selectedX][selectedY] = currentPlayer
            animateStone(selectedX, selectedY)
            if (checkWin(selectedX, selectedY)) {
                winner = currentPlayer; gameOver = true; onWin?.invoke(1000)
            } else {
                currentPlayer = 3 - currentPlayer
            }
        }
    }

    private fun animateStone(x: Int, y: Int) {
        lastPlacedX = x; lastPlacedY = y
        val animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.2f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { stoneScale = it.animatedValue as Float; invalidate() }
        animator.start()
    }

    private fun checkWin(x: Int, y: Int): Boolean {
        val player = board[x][y]
        val dirs = arrayOf(1 to 0, 0 to 1, 1 to 1, 1 to -1)
        for (d in dirs) {
            var count = 1
            var nx = x + d.first; var ny = y + d.second
            while (nx in 0 until size && ny in 0 until size && board[nx][ny] == player) { count++; nx += d.first; ny += d.second }
            nx = x - d.first; ny = y - d.second
            while (nx in 0 until size && ny in 0 until size && board[nx][ny] == player) { count++; nx -= d.first; ny -= d.second }
            if (count >= 5) return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
