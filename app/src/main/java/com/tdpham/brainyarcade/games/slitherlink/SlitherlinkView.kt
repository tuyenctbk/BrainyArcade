package com.tdpham.brainyarcade.games.slitherlink

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

class SlitherlinkView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 5
    private val hEdges = Array(size + 1) { BooleanArray(size) { false } }
    private val vEdges = Array(size) { BooleanArray(size + 1) { false } }
    private val clues = Array(size) { IntArray(size) { -1 } }
    
    private var selectedX = 0
    private var selectedY = 0
    private var horizontalFocus = true
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)

    private val levelPack = listOf(
        listOf(Triple(0, 0, 3), Triple(1, 1, 2), Triple(2, 2, 1), Triple(4, 4, 3)),
        listOf(Triple(0, 1, 3), Triple(0, 3, 3), Triple(4, 1, 3), Triple(4, 3, 3), Triple(2, 2, 0)),
        listOf(Triple(1, 0, 2), Triple(3, 0, 2), Triple(0, 1, 1), Triple(4, 1, 1), Triple(1, 4, 2), Triple(3, 4, 2)),
        listOf(Triple(0, 0, 3), Triple(0, 4, 3), Triple(4, 0, 3), Triple(4, 4, 3), Triple(2, 2, 3)),
        listOf(Triple(1, 1, 2), Triple(1, 3, 2), Triple(3, 1, 2), Triple(3, 3, 2), Triple(2, 0, 1), Triple(2, 4, 1))
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random() else Random(seed)
        val levelData = levelPack[rand.nextInt(levelPack.size)]
        
        for (i in 0 until size) for (j in 0 until size) clues[i][j] = -1
        for (l in levelData) clues[l.first][l.second] = l.third
        
        for (i in 0..size) for (j in 0 until size) hEdges[i][j] = false
        for (i in 0 until size) for (j in 0..size) vEdges[i][j] = false
        
        gameOver = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / (size + 1)
        val offsetX = (width - cellSize * size) / 2
        val offsetY = (height - cellSize * size) / 2

        // Background
        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX - 20, offsetY - 20, offsetX + size * cellSize + 20, offsetY + size * cellSize + 20, 12f, 12f, paint)

        // Clues
        paint.textSize = cellSize * 0.45f
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        val textOffset = (paint.descent() + paint.ascent()) / 2
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (clues[i][j] != -1) {
                    paint.color = if (isClueSatisfied(i, j)) Color.parseColor("#388E3C") else Color.WHITE
                    canvas.drawText(String.format(java.util.Locale.US, "%d", clues[i][j]), offsetX + i * cellSize + cellSize / 2, offsetY + j * cellSize + cellSize / 2 - textOffset, paint)
                }
            }
        }

        // Edges
        paint.strokeWidth = 8f
        paint.strokeCap = Paint.Cap.ROUND
        for (i in 0..size) {
            for (j in 0 until size) {
                paint.color = if (hEdges[i][j]) neonCyan else Color.parseColor("#333333")
                canvas.drawLine(offsetX + j * cellSize, offsetY + i * cellSize, offsetX + (j + 1) * cellSize, offsetY + i * cellSize, paint)
            }
        }
        for (i in 0 until size) {
            for (j in 0..size) {
                paint.color = if (vEdges[i][j]) neonCyan else Color.parseColor("#333333")
                canvas.drawLine(offsetX + j * cellSize, offsetY + i * cellSize, offsetX + j * cellSize, offsetY + (i + 1) * cellSize, paint)
            }
        }

        // Dots
        paint.color = Color.WHITE; paint.style = Paint.Style.FILL
        for (i in 0..size) {
            for (j in 0..size) {
                canvas.drawCircle(offsetX + i * cellSize, offsetY + j * cellSize, 5f, paint)
            }
        }

        // Selection
        if (isFocused && !gameOver) {
            paint.color = Color.WHITE
            if (horizontalFocus) {
                canvas.drawCircle(offsetX + selectedX * cellSize + cellSize / 2, offsetY + selectedY * cellSize, 10f, paint)
            } else {
                canvas.drawCircle(offsetX + selectedX * cellSize, offsetY + selectedY * cellSize + cellSize / 2, 10f, paint)
            }
        }
        
        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f; paint.textAlign = Paint.Align.CENTER
            canvas.drawText("VICTORY!", width/2f, height/2f, paint)
        }
    }

    private fun isClueSatisfied(x: Int, y: Int): Boolean {
        var count = 0
        if (hEdges[y][x]) count++
        if (hEdges[y + 1][x]) count++
        if (vEdges[y][x]) count++
        if (vEdges[y][x + 1]) count++
        return count == clues[x][y]
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (horizontalFocus) {
                    if (selectedY > 0) { selectedY--; horizontalFocus = false } else return false
                } else { horizontalFocus = true }
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (horizontalFocus) { horizontalFocus = false }
                else { if (selectedY < size - 1) { selectedY++; horizontalFocus = true } }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> { if (selectedX > 0) selectedX-- }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { if (selectedX < size - 1) selectedX++ }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> toggleEdge()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun toggleEdge() {
        if (horizontalFocus) {
            hEdges[selectedY][selectedX] = !hEdges[selectedY][selectedX]
        } else {
            vEdges[selectedY][selectedX] = !vEdges[selectedY][selectedX]
        }
        checkWin()
    }

    private fun checkWin() {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (clues[i][j] != -1 && !isClueSatisfied(i, j)) return
            }
        }
        // Simplified loop check
        gameOver = true
        onWin?.invoke(1000)
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
