package com.tdpham.brainyarcade.games.game2048

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.GameView
import kotlin.random.Random

class Game2048View @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val gridSize = 4
    private val grid = Array(gridSize) { IntArray(gridSize) { 0 } }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var gameOver = false
    private var score = 0
    private var mergePopScale = 1.0f
    private var lastMergedX = -1
    private var lastMergedY = -1
    private val history = java.util.Stack<Pair<Array<IntArray>, Int>>()

    var onScoreUpdate: ((Int) -> Unit)? = null
    var onWin: ((Int) -> Unit)? = null
    var onLose: ((Int) -> Unit)? = null

    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val bgDeep = ContextCompat.getColor(context, R.color.background_deep_gray)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random.Default else Random(seed)
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) grid[i][j] = 0
        }
        score = 0
        gameOver = false
        history.clear()
        addRandomTile(rand)
        addRandomTile(rand)
        onScoreUpdate?.invoke(score)
        invalidate()
    }

    override fun undo() {
        if (history.isNotEmpty()) {
            val (prevGrid, prevScore) = history.pop()
            for (i in 0 until gridSize) grid[i] = prevGrid[i].copyOf()
            score = prevScore
            onScoreUpdate?.invoke(score)
            invalidate()
        }
    }

    private fun saveHistory() {
        val snapshot = Array(gridSize) { i -> grid[i].copyOf() }
        history.push(snapshot to score)
    }

    private fun addRandomTile(rand: Random = Random.Default) {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) if (grid[i][j] == 0) emptyCells.add(i to j)
        }
        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells[rand.nextInt(emptyCells.size)]
            grid[r][c] = if (rand.nextFloat() < 0.9) 2 else 4
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / gridSize
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        paint.color = bgDeep
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)

        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                val value = grid[i][j]
                val left = offsetX + i * cellSize + 8
                val top = offsetY + j * cellSize + 8
                val right = left + cellSize - 16
                val bottom = top + cellSize - 16
                
                paint.color = getTileColor(value)
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(left, top, right, bottom, 12f, 12f, paint)

                if (value != 0) {
                    val scale = if (i == lastMergedX && j == lastMergedY) mergePopScale else 1.0f
                    paint.color = if (value <= 4) Color.DKGRAY else Color.WHITE
                    paint.textSize = cellSize * 0.35f * scale
                    paint.textAlign = Paint.Align.CENTER
                    val textOffset = (paint.descent() + paint.ascent()) / 2
                    canvas.drawText(String.format(java.util.Locale.US, "%d", value), left + (cellSize - 16) / 2, top + (cellSize - 16) / 2 - textOffset, paint)
                }
            }
        }
        
        if (isFocused) {
            paint.color = neonCyan
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f
            paint.setShadowLayer(15f, 0f, 0f, neonCyan)
            canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)
            paint.clearShadowLayer()
        }
    }

    private fun getTileColor(value: Int): Int {
        return when (value) {
            0 -> Color.parseColor("#2A2A2A")
            2 -> Color.parseColor("#EEE4DA")
            4 -> Color.parseColor("#EDE0C8")
            8 -> Color.parseColor("#F2B179")
            16 -> Color.parseColor("#F59563")
            32 -> Color.parseColor("#F67C5F")
            64 -> Color.parseColor("#F65E3B")
            128 -> Color.parseColor("#EDCF72")
            256 -> Color.parseColor("#EDCC61")
            512 -> Color.parseColor("#EDC850")
            1024 -> Color.parseColor("#EDC53F")
            2048 -> Color.parseColor("#EDC22E")
            else -> Color.parseColor("#3C3A32")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        var moved = false
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                saveHistory()
                moved = moveUp()
                if (!moved) return false // Focus escape
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> { saveHistory(); moved = moveDown() }
            KeyEvent.KEYCODE_DPAD_LEFT -> { saveHistory(); moved = moveLeft() }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { saveHistory(); moved = moveRight() }
            else -> return super.onKeyDown(keyCode, event)
        }
        if (moved) {
            addRandomTile()
            checkGameOver()
            onScoreUpdate?.invoke(score)
            invalidate()
        }
        return true
    }

    private fun animateMerge(x: Int, y: Int) {
        lastMergedX = x
        lastMergedY = y
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, 1.2f, 1.0f)
        animator.duration = 150
        animator.addUpdateListener { 
            mergePopScale = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun moveLeft(): Boolean {
        var moved = false
        for (j in 0 until gridSize) {
            val row = IntArray(gridSize) { i -> grid[i][j] }
            val (newRow, m) = slideAndMerge(row, j, true, false)
            if (m) moved = true
            for (i in 0 until gridSize) grid[i][j] = newRow[i]
        }
        return moved
    }

    private fun moveRight(): Boolean {
        var moved = false
        for (j in 0 until gridSize) {
            val row = IntArray(gridSize) { i -> grid[gridSize - 1 - i][j] }
            val (newRow, m) = slideAndMerge(row, j, true, true)
            if (m) moved = true
            for (i in 0 until gridSize) grid[gridSize - 1 - i][j] = newRow[i]
        }
        return moved
    }

    private fun moveUp(): Boolean {
        var moved = false
        for (i in 0 until gridSize) {
            val col = IntArray(gridSize) { j -> grid[i][j] }
            val (newCol, m) = slideAndMerge(col, i, false, false)
            if (m) moved = true
            for (j in 0 until gridSize) grid[i][j] = newCol[j]
        }
        return moved
    }

    private fun moveDown(): Boolean {
        var moved = false
        for (i in 0 until gridSize) {
            val col = IntArray(gridSize) { j -> grid[i][gridSize - 1 - j] }
            val (newCol, m) = slideAndMerge(col, i, false, true)
            if (m) moved = true
            for (j in 0 until gridSize) grid[i][gridSize - 1 - j] = newCol[j]
        }
        return moved
    }

    private fun slideAndMerge(line: IntArray, lineIdx: Int, isHorizontal: Boolean, isReversed: Boolean): Pair<IntArray, Boolean> {
        val result = IntArray(gridSize) { 0 }
        var targetIdx = 0
        val temp = line.filter { it != 0 }
        var i = 0
        var moved = false
        if (temp.size != line.count { it != 0 }) moved = true

        while (i < temp.size) {
            if (i + 1 < temp.size && temp[i] == temp[i + 1]) {
                result[targetIdx] = temp[i] * 2
                score += result[targetIdx]
                
                val finalIdx = if (isReversed) gridSize - 1 - targetIdx else targetIdx
                if (isHorizontal) animateMerge(finalIdx, lineIdx) else animateMerge(lineIdx, finalIdx)
                
                i += 2
                moved = true
            } else {
                result[targetIdx] = temp[i]
                i++
            }
            targetIdx++
        }
        if (!line.contentEquals(result)) moved = true
        return result to moved
    }

    private fun checkGameOver() {
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                if (grid[i][j] == 2048) {
                    gameOver = true
                    onWin?.invoke(score)
                    return
                }
                if (grid[i][j] == 0) return
                if (i < gridSize - 1 && grid[i][j] == grid[i + 1][j]) return
                if (j < gridSize - 1 && grid[i][j] == grid[i][j + 1]) return
            }
        }
        gameOver = true
        onLose?.invoke(score)
    }

    private var startX = 0f
    private var startY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                requestFocus()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val diffX = event.x - startX
                val diffY = event.y - startY
                val swipeThreshold = 80
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > swipeThreshold) {
                        var moved = false
                        if (diffX > 0) {
                            saveHistory()
                            moved = moveRight()
                        } else {
                            saveHistory()
                            moved = moveLeft()
                        }
                        if (moved) {
                            addRandomTile()
                            checkGameOver()
                            onScoreUpdate?.invoke(score)
                            invalidate()
                        }
                        performClick()
                        return true
                    }
                } else {
                    if (Math.abs(diffY) > swipeThreshold) {
                        var moved = false
                        if (diffY > 0) {
                            saveHistory()
                            moved = moveDown()
                        } else {
                            saveHistory()
                            moved = moveUp()
                        }
                        if (moved) {
                            addRandomTile()
                            checkGameOver()
                            onScoreUpdate?.invoke(score)
                            invalidate()
                        }
                        performClick()
                        return true
                    }
                }
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
