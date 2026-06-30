package com.tdpham.brainyarcade.games.starbattle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class StarBattleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 6
    private val board = Array(size) { IntArray(size) { 0 } }
    data class Region(val cells: List<Pair<Int, Int>>)
    private val regions = mutableListOf<Region>()
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
        regions.add(Region(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1)))
        regions.add(Region(listOf(2 to 0, 3 to 0, 2 to 1, 3 to 1, 4 to 0, 5 to 0)))
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (regions.none { it.cells.contains(i to j) }) {
                    regions.add(Region(listOf(i to j)))
                }
            }
        }
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) {
            for (j in 0 until size) board[i][j] = 0
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

                paint.color = Color.DKGRAY
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRect(left, top, right, bottom, paint)

                paint.style = Paint.Style.FILL
                if (board[i][j] == 1) {
                    paint.color = Color.YELLOW
                    drawStar(canvas, left + cellSize / 2, top + cellSize / 2, cellSize * 0.35f)
                } else if (board[i][j] == 2) {
                    paint.color = Color.LTGRAY
                    canvas.drawCircle(left + cellSize / 2, top + cellSize / 2, cellSize * 0.1f, paint)
                }
            }
        }

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        for (region in regions) {
            for (cell in region.cells) {
                val left = offsetX + cell.first * cellSize
                val top = offsetY + cell.second * cellSize
                val right = left + cellSize
                val bottom = top + cellSize
                if (!region.cells.contains(cell.first - 1 to cell.second)) canvas.drawLine(left, top, left, bottom, paint)
                if (!region.cells.contains(cell.first + 1 to cell.second)) canvas.drawLine(right, top, right, bottom, paint)
                if (!region.cells.contains(cell.first to cell.second - 1)) canvas.drawLine(left, top, right, top, paint)
                if (!region.cells.contains(cell.first to cell.second + 1)) canvas.drawLine(left, bottom, right, bottom, paint)
            }
        }

        if (isFocused && !gameOver) {
            paint.color = Color.parseColor("#00BCD4")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            canvas.drawRect(offsetX + selectedX * cellSize + 2, offsetY + selectedY * cellSize + 2, 
                offsetX + (selectedX + 1) * cellSize - 2, offsetY + (selectedY + 1) * cellSize - 2, paint)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val p = android.graphics.Path()
        val innerRadius = radius / 2.5f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = Math.PI * i / 5 - Math.PI / 2
            val x = (cx + r * Math.cos(angle)).toFloat()
            val y = (cy + r * Math.sin(angle)).toFloat()
            if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        canvas.drawPath(p, paint)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> selectedY = (selectedY - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> toggle(1)
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_D -> toggle(2)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun toggle(type: Int) {
        board[selectedX][selectedY] = if (board[selectedX][selectedY] == type) 0 else type
        checkWin()
    }

    private fun checkWin() {
        // Simplified check
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
