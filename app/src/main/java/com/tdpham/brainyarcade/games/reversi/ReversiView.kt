package com.tdpham.brainyarcade.games.reversi

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

class ReversiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 8
    private val board = Array(size) { IntArray(size) { 0 } }
    private var currentPlayer = 2 
    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    
    private var flipScale = 1.0f
    private val flippingTiles = mutableListOf<Pair<Int, Int>>()

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) for (j in 0 until size) board[i][j] = 0
        board[3][3] = 1; board[4][4] = 1; board[3][4] = 2; board[4][3] = 2
        currentPlayer = 2
        gameOver = false
        flippingTiles.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)

        paint.color = Color.parseColor("#333333")
        paint.strokeWidth = 2f; paint.style = Paint.Style.STROKE
        for (i in 0..size) {
            canvas.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + availableSize, paint)
            canvas.drawLine(offsetX, offsetY + i * cellSize, offsetX + availableSize, offsetY + i * cellSize, paint)
        }

        if (isFocused && !gameOver) {
            paint.color = neonCyan; paint.strokeWidth = 4f
            canvas.drawRect(offsetX + selectedX * cellSize + 4, offsetY + selectedY * cellSize + 4, 
                offsetX + (selectedX + 1) * cellSize - 4, offsetY + (selectedY + 1) * cellSize - 4, paint)
        }

        paint.style = Paint.Style.FILL
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] != 0) {
                    val scale = if (flippingTiles.contains(i to j)) flipScale else 1.0f
                    paint.color = if (board[i][j] == 1) Color.WHITE else Color.BLACK
                    canvas.drawCircle(offsetX + i * cellSize + cellSize / 2, 
                        offsetY + j * cellSize + cellSize / 2, cellSize * 0.4f * Math.abs(scale), paint)
                }
            }
        }
        
        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f; paint.textAlign = Paint.Align.CENTER
            canvas.drawText("GAME OVER", width / 2f, height / 2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || flippingTiles.isNotEmpty()) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { if (selectedY == 0) return false; selectedY = (selectedY - 1 + size) % size }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> makeMove(selectedX, selectedY)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun makeMove(x: Int, y: Int) {
        if (board[x][y] != 0) return
        val flips = getFlips(x, y, currentPlayer)
        if (flips.isNotEmpty()) {
            board[x][y] = currentPlayer
            animateFlips(flips, currentPlayer)
        }
    }

    private fun animateFlips(flips: List<Pair<Int, Int>>, player: Int) {
        flippingTiles.clear(); flippingTiles.addAll(flips)
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, -1.0f)
        animator.duration = 400
        animator.addUpdateListener { 
            flipScale = it.animatedValue as Float
            if (flipScale < 0 && board[flippingTiles[0].first][flippingTiles[0].second] != player) {
                for (f in flippingTiles) board[f.first][f.second] = player
            }
            invalidate()
        }
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                flippingTiles.clear()
                currentPlayer = 3 - currentPlayer
                checkGameOver()
                invalidate()
            }
        })
        animator.start()
    }

    private fun getFlips(x: Int, y: Int, player: Int): List<Pair<Int, Int>> {
        val flips = mutableListOf<Pair<Int, Int>>()
        val opponent = 3 - player
        val dirs = arrayOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
        for (d in dirs) {
            val dirFlips = mutableListOf<Pair<Int, Int>>()
            var cx = x + d.first; var cy = y + d.second
            while (cx in 0 until size && cy in 0 until size && board[cx][cy] == opponent) {
                dirFlips.add(cx to cy); cx += d.first; cy += d.second
            }
            if (cx in 0 until size && cy in 0 until size && board[cx][cy] == player) flips.addAll(dirFlips)
        }
        return flips
    }

    private fun checkGameOver() {
        var emptyCount = 0; var p1 = 0; var p2 = 0
        for (i in 0 until size) for (j in 0 until size) {
            if (board[i][j] == 0) emptyCount++
            else if (board[i][j] == 1) p1++ else p2++
        }
        if (emptyCount == 0 || (getValidMoves(1).isEmpty() && getValidMoves(2).isEmpty())) {
            gameOver = true; onWin?.invoke(p2)
        } else if (getValidMoves(currentPlayer).isEmpty()) {
            currentPlayer = 3 - currentPlayer // Pass turn
        }
    }

    private fun getValidMoves(player: Int): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until size) for (j in 0 until size) {
            if (board[i][j] == 0 && getFlips(i, j, player).isNotEmpty()) moves.add(i to j)
        }
        return moves
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
