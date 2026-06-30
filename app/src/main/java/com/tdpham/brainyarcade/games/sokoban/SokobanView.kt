package com.tdpham.brainyarcade.games.sokoban

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
import java.util.*

class SokobanView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val levelPack = listOf(
        // Level 1
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 4, 0, 0, 0, 0, 1),
            intArrayOf(1, 0, 3, 2, 3, 0, 1),
            intArrayOf(1, 0, 0, 2, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 1)
        ),
        // Level 2
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1),
            intArrayOf(1, 2, 4, 0, 1, 1),
            intArrayOf(1, 0, 3, 3, 2, 1),
            intArrayOf(1, 0, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1)
        ),
        // Level 3
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1),
            intArrayOf(1, 2, 0, 0, 1),
            intArrayOf(1, 3, 4, 0, 1),
            intArrayOf(1, 1, 1, 1, 1)
        ),
        // Level 4
        arrayOf(
            intArrayOf(0, 1, 1, 1, 0),
            intArrayOf(1, 1, 2, 1, 1),
            intArrayOf(1, 4, 3, 0, 1),
            intArrayOf(1, 1, 1, 1, 1)
        ),
        // Level 5
        arrayOf(
            intArrayOf(1, 1, 1, 1, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 2, 0, 1),
            intArrayOf(1, 0, 3, 4, 0, 0, 1),
            intArrayOf(1, 1, 1, 1, 1, 1, 1)
        )
    )
    
    private var grid = Array(0) { intArrayOf() }
    private var playerX = 1
    private var playerY = 1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onMove: ((Int) -> Unit)? = null
    private var moves = 0
    private var animPlayerX = 1f
    private var animPlayerY = 1f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random() else Random(seed)
        val baseLevel = levelPack[rand.nextInt(levelPack.size)]
        
        grid = Array(baseLevel.size) { r -> baseLevel[r].copyOf() }
        findPlayer()
        animPlayerX = playerX.toFloat()
        animPlayerY = playerY.toFloat()
        gameOver = false
        moves = 0
        invalidate()
    }

    private fun findPlayer() {
        for (y in grid.indices) for (x in grid[y].indices) {
            if (grid[y][x] == 4 || grid[y][x] == 6) {
                playerX = x; playerY = y; return
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (grid.isEmpty()) return
        
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / Math.max(grid.size, grid[0].size)
        val offsetX = (width - cellSize * grid[0].size) / 2
        val offsetY = (height - cellSize * grid.size) / 2

        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX, offsetY, offsetX + cellSize * grid[0].size, offsetY + cellSize * grid.size, 8f, 8f, paint)

        for (y in grid.indices) {
            for (x in grid[y].indices) {
                val left = offsetX + x * cellSize; val top = offsetY + y * cellSize
                val right = left + cellSize; val bottom = top + cellSize

                when (grid[y][x]) {
                    1 -> { paint.color = Color.parseColor("#333333"); canvas.drawRect(left, top, right, bottom, paint) }
                    2 -> { paint.color = Color.RED; canvas.drawCircle(left + cellSize/2, top + cellSize/2, cellSize * 0.15f, paint) }
                    3 -> { paint.color = Color.parseColor("#A0522D"); canvas.drawRoundRect(left + 8, top + 8, right - 8, bottom - 8, 8f, 8f, paint) }
                    5 -> { paint.color = Color.GREEN; canvas.drawRoundRect(left + 8, top + 8, right - 8, bottom - 8, 8f, 8f, paint) }
                }
            }
        }

        // Draw Animated Player
        val pLeft = offsetX + animPlayerX * cellSize
        val pTop = offsetY + animPlayerY * cellSize
        paint.color = neonCyan
        canvas.drawCircle(pLeft + cellSize/2, pTop + cellSize/2, cellSize * 0.35f, paint)
        if (grid[playerY][playerX] == 6) {
             paint.color = Color.RED; paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f
             canvas.drawCircle(pLeft + cellSize/2, pTop + cellSize/2, cellSize * 0.15f, paint); paint.style = Paint.Style.FILL
        }
        
        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f; paint.textAlign = Paint.Align.CENTER
            canvas.drawText("VICTORY!", width/2f, height/2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (playerY == 0) return false
                move(0, -1)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> move(0, 1)
            KeyEvent.KEYCODE_DPAD_LEFT -> move(-1, 0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> move(1, 0)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun move(dx: Int, dy: Int) {
        val nx = playerX + dx; val ny = playerY + dy
        if (ny !in grid.indices || nx !in grid[ny].indices) return

        val target = grid[ny][nx]
        if (target == 0 || target == 2) {
            grid[playerY][playerX] = if (grid[playerY][playerX] == 6) 2 else 0
            grid[ny][nx] = if (target == 2) 6 else 4
            playerX = nx; playerY = ny; moves++; onMove?.invoke(moves)
            animatePlayer()
        } else if (target == 3 || target == 5) {
            val nnx = nx + dx; val nny = ny + dy
            if (nny in grid.indices && nnx in grid[nny].indices) {
                val nextTarget = grid[nny][nnx]
                if (nextTarget == 0 || nextTarget == 2) {
                    grid[nny][nnx] = if (nextTarget == 2) 5 else 3
                    grid[ny][nx] = if (target == 5) 6 else 4
                    grid[playerY][playerX] = if (grid[playerY][playerX] == 6) 2 else 0
                    playerX = nx; playerY = ny; moves++; onMove?.invoke(moves)
                    animatePlayer()
                }
            }
        }
        checkWin()
    }

    private fun animatePlayer() {
        val animX = android.animation.ValueAnimator.ofFloat(animPlayerX, playerX.toFloat())
        val animY = android.animation.ValueAnimator.ofFloat(animPlayerY, playerY.toFloat())
        val set = android.animation.AnimatorSet()
        set.playTogether(animX, animY)
        set.duration = 150
        animX.addUpdateListener { animPlayerX = it.animatedValue as Float; invalidate() }
        animY.addUpdateListener { animPlayerY = it.animatedValue as Float; invalidate() }
        set.start()
    }

    private fun checkWin() {
        if (grid.none { row -> row.contains(3) }) { gameOver = true; onWin?.invoke(moves) }
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
