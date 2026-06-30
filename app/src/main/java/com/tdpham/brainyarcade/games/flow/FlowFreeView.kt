package com.tdpham.brainyarcade.games.flow

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.GameView
import java.util.*

class FlowFreeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 5
    private val board = Array(size) { IntArray(size) { 0 } }
    private val paths = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
    
    private val colorMap = mapOf(
        1 to Color.parseColor("#E50914"), // Red
        2 to Color.parseColor("#00BCD4"), // Cyan
        3 to Color.parseColor("#FFD700"), // Gold
        4 to Color.parseColor("#32CD32"), // Green
        5 to Color.parseColor("#9370DB")  // Purple
    )

    private var selectedX = 0
    private var selectedY = 0
    private var activePathColor = 0
    private var gameOver = false
    private val history = Stack<Map<Int, List<Pair<Int, Int>>>>()
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    
    private val preDefinedLevels = listOf(
        listOf(Triple(0, 0, 1), Triple(4, 4, 1), Triple(0, 4, 2), Triple(4, 0, 2), Triple(2, 2, 3), Triple(3, 3, 3)),
        listOf(Triple(0, 0, 1), Triple(0, 2, 1), Triple(1, 1, 2), Triple(3, 1, 2), Triple(2, 0, 3), Triple(4, 4, 3), Triple(4, 0, 4), Triple(2, 4, 4)),
        listOf(Triple(0, 0, 1), Triple(4, 0, 1), Triple(0, 1, 2), Triple(4, 1, 2), Triple(0, 2, 3), Triple(4, 2, 3), Triple(0, 3, 4), Triple(4, 3, 4), Triple(0, 4, 5), Triple(4, 4, 5)),
        listOf(Triple(0, 0, 1), Triple(2, 2, 1), Triple(1, 0, 2), Triple(3, 3, 2), Triple(4, 0, 3), Triple(4, 4, 3), Triple(0, 4, 4), Triple(2, 4, 4)),
        listOf(Triple(0, 1, 1), Triple(4, 1, 1), Triple(0, 2, 2), Triple(4, 2, 2), Triple(0, 3, 3), Triple(4, 3, 3), Triple(2, 0, 4), Triple(2, 4, 4)),
        listOf(Triple(0, 0, 1), Triple(1, 1, 1), Triple(0, 4, 2), Triple(1, 3, 2), Triple(4, 4, 3), Triple(3, 3, 3), Triple(4, 0, 4), Triple(3, 1, 4)),
        listOf(Triple(1, 1, 1), Triple(3, 3, 1), Triple(0, 0, 2), Triple(4, 4, 2), Triple(4, 0, 3), Triple(0, 4, 3), Triple(2, 0, 4), Triple(2, 4, 4)),
        listOf(Triple(0, 0, 1), Triple(0, 4, 1), Triple(1, 0, 2), Triple(1, 4, 2), Triple(2, 0, 3), Triple(2, 4, 3), Triple(3, 0, 4), Triple(3, 4, 4), Triple(4, 0, 5), Triple(4, 4, 5)),
        listOf(Triple(0, 0, 1), Triple(2, 0, 1), Triple(0, 1, 2), Triple(2, 1, 2), Triple(0, 2, 3), Triple(2, 2, 3), Triple(0, 3, 4), Triple(2, 3, 4), Triple(0, 4, 5), Triple(2, 4, 5)),
        listOf(Triple(4, 0, 1), Triple(4, 4, 1), Triple(3, 0, 2), Triple(3, 4, 2), Triple(2, 0, 3), Triple(2, 4, 3), Triple(1, 0, 4), Triple(1, 4, 4), Triple(0, 0, 5), Triple(0, 4, 5))
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random() else Random(seed)
        
        // Pick a random level but also randomize colors
        val levelDataRaw = preDefinedLevels[rand.nextInt(preDefinedLevels.size)]
        val colorMapping = (1..5).shuffled(rand)
        
        for (i in 0 until size) for (j in 0 until size) board[i][j] = 0
        paths.clear()
        
        for (l in levelDataRaw) {
            val mappedColor = if (l.third <= colorMapping.size) colorMapping[l.third - 1] else l.third
            board[l.first][l.second] = mappedColor
        }
        
        selectedX = 0; selectedY = 0
        activePathColor = 0
        gameOver = false
        history.clear()
        invalidate()
    }

    override fun undo() {
        if (history.isNotEmpty()) {
            val prev = history.pop()
            paths.clear()
            prev.forEach { (c, p) -> paths[c] = p.toMutableList() }
            invalidate()
        }
    }

    private fun saveHistory() {
        val snapshot = paths.mapValues { it.value.toList() }
        history.push(snapshot)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        // Background
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#121212")
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)

        // Grid
        paint.color = Color.parseColor("#333333")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        for (i in 0..size) {
            canvas.drawLine(offsetX + i * cellSize, offsetY, offsetX + i * cellSize, offsetY + availableSize, paint)
            canvas.drawLine(offsetX, offsetY + i * cellSize, offsetX + availableSize, offsetY + i * cellSize, paint)
        }

        // Draw paths
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = cellSize * 0.4f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        for ((colorId, path) in paths) {
            if (path.size < 2) continue
            paint.color = colorMap[colorId] ?: Color.WHITE
            val p = Path()
            p.moveTo(offsetX + path[0].first * cellSize + cellSize/2, offsetY + path[0].second * cellSize + cellSize/2)
            for (i in 1 until path.size) {
                p.lineTo(offsetX + path[i].first * cellSize + cellSize/2, offsetY + path[i].second * cellSize + cellSize/2)
            }
            canvas.drawPath(p, paint)
        }

        // Endpoints
        paint.style = Paint.Style.FILL
        for (i in 0 until size) {
            for (j in 0 until size) {
                val colorId = board[i][j]
                if (colorId != 0) {
                    paint.color = colorMap[colorId] ?: Color.WHITE
                    canvas.drawCircle(offsetX + i * cellSize + cellSize/2, offsetY + j * cellSize + cellSize/2, cellSize * 0.35f, paint)
                    // Inner dot for endpoints
                    paint.color = Color.parseColor("#33000000")
                    canvas.drawCircle(offsetX + i * cellSize + cellSize/2, offsetY + j * cellSize + cellSize/2, cellSize * 0.1f, paint)
                }
            }
        }

        // Selection
        if (isFocused && !gameOver) {
            paint.style = Paint.Style.STROKE; paint.color = neonCyan; paint.strokeWidth = 6f
            canvas.drawRoundRect(offsetX + selectedX * cellSize + 8, offsetY + selectedY * cellSize + 8,
                offsetX + (selectedX + 1) * cellSize - 8, offsetY + (selectedY + 1) * cellSize - 8, 8f, 8f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> handleMove(0, -1)
            KeyEvent.KEYCODE_DPAD_DOWN -> handleMove(0, 1)
            KeyEvent.KEYCODE_DPAD_LEFT -> handleMove(-1, 0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> handleMove(1, 0)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val colorAt = board[selectedX][selectedY]
                if (colorAt != 0) {
                    saveHistory()
                    activePathColor = colorAt
                    paths[colorAt] = mutableListOf(selectedX to selectedY)
                } else {
                    // Check if they clicked on an existing path to start drawing from there
                    val foundColor = paths.entries.find { it.value.contains(selectedX to selectedY) }?.key
                    if (foundColor != null) {
                        saveHistory()
                        activePathColor = foundColor
                        val path = paths[foundColor]!!
                        val idx = path.indexOf(selectedX to selectedY)
                        while (path.size > idx + 1) path.removeAt(path.size - 1)
                    } else {
                        activePathColor = 0
                    }
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun handleMove(dx: Int, dy: Int) {
        val nx = selectedX + dx
        val ny = selectedY + dy
        
        if (nx !in 0 until size || ny !in 0 until size) return

        selectedX = nx; selectedY = ny
        
        if (activePathColor != 0) {
            val path = paths[activePathColor]!!
            val pos = nx to ny
            
            if (path.contains(pos)) {
                // Backtrack
                val idx = path.indexOf(pos)
                while (path.size > idx + 1) path.removeAt(path.size - 1)
            } else {
                // Check for collisions with other paths
                for ((cid, otherPath) in paths) {
                    if (cid == activePathColor) continue
                    if (otherPath.contains(pos)) {
                        val oIdx = otherPath.indexOf(pos)
                        while (otherPath.size > oIdx) otherPath.removeAt(otherPath.size - 1)
                    }
                }
                
                path.add(pos)
                
                // Reached an endpoint
                if (board[nx][ny] != 0) {
                    if (board[nx][ny] == activePathColor) {
                        activePathColor = 0
                        checkWin()
                    } else {
                        activePathColor = 0 // Blocked by wrong endpoint
                    }
                }
            }
        }
    }

    private fun checkWin() {
        val endpoints = mutableMapOf<Int, Int>()
        for (i in 0 until size) for (j in 0 until size) {
            if (board[i][j] != 0) endpoints[board[i][j]] = (endpoints[board[i][j]] ?: 0) + 1
        }
        
        for (color in endpoints.keys) {
            val path = paths[color]
            if (path == null || path.size < 2 || board[path.first().first][path.first().second] != color || board[path.last().first][path.last().second] != color) {
                return
            }
        }
        
        gameOver = true
        onWin?.invoke(1000)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) return false
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2
        
        val x = ((event.x - offsetX) / cellSize).toInt()
        val y = ((event.y - offsetY) / cellSize).toInt()
        
        if (x !in 0 until size || y !in 0 until size) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedX = x; selectedY = y
                val colorAt = board[x][y]
                if (colorAt != 0) {
                    activePathColor = colorAt
                    paths[colorAt] = mutableListOf(x to y)
                    invalidate()
                    requestFocus()
                } else {
                    val foundColor = paths.entries.find { it.value.contains(x to y) }?.key
                    if (foundColor != null) {
                        activePathColor = foundColor
                        val path = paths[foundColor]!!
                        val idx = path.indexOf(x to y)
                        while (path.size > idx + 1) path.removeAt(path.size - 1)
                        invalidate()
                        requestFocus()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePathColor != 0 && (x != selectedX || y != selectedY)) {
                    if (Math.abs(x - selectedX) + Math.abs(y - selectedY) == 1) {
                        handleMove(x - selectedX, y - selectedY)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                activePathColor = 0
            }
        }
        return true
    }
}
