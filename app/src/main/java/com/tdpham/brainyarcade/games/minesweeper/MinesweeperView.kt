package com.tdpham.brainyarcade.games.minesweeper

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
import kotlin.random.Random

class MinesweeperView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val cols = 10
    private val rows = 10
    private val mineCount = 15
    private val grid = Array(cols) { IntArray(rows) { 0 } }
    private val revealed = Array(cols) { BooleanArray(rows) { false } }
    private val flagged = Array(cols) { BooleanArray(rows) { false } }
    private var selectedX = 0
    private var selectedY = 0
    private var gameOver = false
    private var win = false
    private var firstClick = true
    private var gameSeed: Long = -1
    
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private var revealScale = 0.0f
    private val revealingPositions = mutableSetOf<Pair<Int, Int>>()

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        gameSeed = seed
        // Difficulty: Increase mine count with level
        // mineCount = 15 + level/5
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                grid[i][j] = 0
                revealed[i][j] = false
                flagged[i][j] = false
            }
        }
        gameOver = false
        win = false
        firstClick = true
        invalidate()
    }

    private fun placeMines(startX: Int, startY: Int) {
        val rand = if (gameSeed == -1L) Random.Default else Random(gameSeed)
        var minesPlaced = 0
        while (minesPlaced < mineCount) {
            val x = rand.nextInt(cols)
            val y = rand.nextInt(rows)
            
            // Don't place mine on first click or its adjacent cells
            if (grid[x][y] != -1 && (Math.abs(x - startX) > 1 || Math.abs(y - startY) > 1)) {
                grid[x][y] = -1
                minesPlaced++
            }
        }
        
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                if (grid[i][j] == -1) continue
                var count = 0
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        val nx = i + dx; val ny = j + dy
                        if (nx in 0 until cols && ny in 0 until rows && grid[nx][ny] == -1) count++
                    }
                }
                grid[i][j] = count
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / Math.max(cols, rows)
        val offsetX = (width - cellSize * cols) / 2
        val offsetY = (height - cellSize * rows) / 2

        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX, offsetY, offsetX + cellSize * cols, offsetY + cellSize * rows, 8f, 8f, paint)

        for (i in 0 until cols) {
            for (j in 0 until rows) {
                val left = offsetX + i * cellSize + 2
                val top = offsetY + j * cellSize + 2
                val right = left + cellSize - 4
                val bottom = top + cellSize - 4

                if (revealed[i][j]) {
                    paint.style = Paint.Style.FILL
                    paint.color = if (grid[i][j] == -1) Color.RED else Color.parseColor("#E0E0E0")
                    canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, paint)
                    
                    if (grid[i][j] > 0) {
                        val scale = if (revealingPositions.contains(i to j)) revealScale else 1.0f
                        paint.color = getNumberColor(grid[i][j])
                        paint.textSize = cellSize * 0.6f * scale
                        paint.textAlign = Paint.Align.CENTER
                        val textOffset = (paint.descent() + paint.ascent()) / 2
                        canvas.drawText(String.format(java.util.Locale.US, "%d", grid[i][j]), left + (cellSize-4)/2, top + (cellSize-4)/2 - textOffset, paint)
                    }
                } else {
                    paint.style = Paint.Style.FILL
                    paint.color = Color.parseColor("#333333")
                    canvas.drawRoundRect(left, top, right, bottom, 4f, 4f, paint)
                    if (flagged[i][j]) {
                        paint.color = ContextCompat.getColor(context, R.color.gold)
                        canvas.drawCircle(left + (cellSize-4)/2, top + (cellSize-4)/2, cellSize * 0.2f, paint)
                    }
                }

                if (i == selectedX && j == selectedY && isFocused && !gameOver && !win) {
                    paint.style = Paint.Style.STROKE
                    paint.color = neonCyan
                    paint.strokeWidth = 4f
                    paint.setShadowLayer(10f, 0f, 0f, neonCyan)
                    canvas.drawRoundRect(left - 1, top - 1, right + 1, bottom + 1, 6f, 6f, paint)
                    paint.clearShadowLayer()
                }
            }
        }
        
        if (gameOver || win) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(if (win) "VICTORY!" else "GAME OVER", width / 2f, height / 2f, paint)
        }
    }

    private fun getNumberColor(n: Int): Int {
        return when (n) {
            1 -> Color.BLUE
            2 -> Color.parseColor("#388E3C")
            3 -> Color.RED
            4 -> Color.parseColor("#191970")
            5 -> Color.parseColor("#8B0000")
            6 -> Color.CYAN
            7 -> Color.BLACK
            else -> Color.GRAY
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || win) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { if (selectedY == 0) return false; selectedY = (selectedY - 1 + rows) % rows }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % rows
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + cols) % cols
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % cols
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> reveal(selectedX, selectedY)
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_X -> toggleFlag(selectedX, selectedY)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun reveal(x: Int, y: Int) {
        if (flagged[x][y] || revealed[x][y]) return
        
        if (firstClick) {
            firstClick = false
            placeMines(x, y)
        }

        revealed[x][y] = true
        animateReveal(x, y)
        if (grid[x][y] == -1) {
            shake()
            gameOver = true
            revealAllMines()
            onLose?.invoke()
        } else if (grid[x][y] == 0) {
            revealEmpty(x, y)
        }
        checkWin()
    }

    private fun shake() {
        this.animate()
            .translationX(10f)
            .setDuration(300)
            .setInterpolator(android.view.animation.CycleInterpolator(4f))
            .start()
    }

    private fun animateReveal(x: Int, y: Int) {
        val pos = x to y
        revealingPositions.add(pos)
        val animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 250
        animator.addUpdateListener { 
            revealScale = it.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                revealingPositions.remove(pos)
            }
        })
        animator.start()
    }

    private fun revealEmpty(x: Int, y: Int) {
        for (dx in -1..1) {
            for (dy in -1..1) {
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until cols && ny in 0 until rows && !revealed[nx][ny]) {
                    reveal(nx, ny)
                }
            }
        }
    }

    private fun toggleFlag(x: Int, y: Int) {
        if (!revealed[x][y]) flagged[x][y] = !flagged[x][y]
    }

    private fun revealAllMines() {
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                if (grid[i][j] == -1) revealed[i][j] = true
            }
        }
    }

    private fun checkWin() {
        var count = 0
        for (i in 0 until cols) {
            for (j in 0 until rows) if (revealed[i][j]) count++
        }
        if (count == cols * rows - mineCount) {
            win = true
            onWin?.invoke(mineCount * 10)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver && !win) {
            performClick()
            val availableSize = Math.min(width, height).toFloat()
            val cellSize = availableSize / Math.max(cols, rows)
            val offsetX = (width - cellSize * cols) / 2
            val offsetY = (height - cellSize * rows) / 2
            val x = ((event.x - offsetX) / cellSize).toInt()
            val y = ((event.y - offsetY) / cellSize).toInt()
            if (x in 0 until cols && y in 0 until rows) {
                selectedX = x; selectedY = y
                reveal(x, y)
                invalidate()
                requestFocus()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
