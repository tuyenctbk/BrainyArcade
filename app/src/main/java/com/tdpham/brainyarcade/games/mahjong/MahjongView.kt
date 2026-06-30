package com.tdpham.brainyarcade.games.mahjong

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

class MahjongView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    data class Tile(val id: Int, val x: Int, val y: Int, val z: Int, var matched: Boolean = false)

    private val tiles = mutableListOf<Tile>()
    private var selectedTile: Tile? = null
    private var focusedTileIdx = 0
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val textWhite = ContextCompat.getColor(context, R.color.text_white)
    
    private var matchAnimScale = 1.0f
    private var lastMatchedId = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random.Default else Random(seed)
        tiles.clear()
        val ids = mutableListOf<Int>()
        for (i in 0 until 10) repeat(4) { ids.add(i) }
        ids.shuffle(rand)

        var idx = 0
        for (z in 0..1) {
            for (y in 0..3) {
                for (x in 0..4) {
                    if (idx < ids.size) {
                        tiles.add(Tile(ids[idx++], x, y, z))
                    }
                }
            }
        }
        gameOver = false
        selectedTile = null
        focusedTileIdx = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = Math.min(width / 7f, height / 6f)
        val offsetX = (width - cellSize * 5) / 2
        val offsetY = (height - cellSize * 4) / 2

        val activeTiles = tiles.filter { !it.matched }.sortedWith(compareBy({ it.z }, { it.y }, { it.x }))

        for (tile in activeTiles) {
            val left = offsetX + tile.x * cellSize + tile.z * 10f
            val top = offsetY + tile.y * cellSize - tile.z * 10f
            val right = left + cellSize - 20f
            val bottom = top + cellSize - 20f

            // Shadow
            paint.color = Color.BLACK
            canvas.drawRoundRect(left + 5, top + 5, right + 5, bottom + 5, 12f, 12f, paint)

            // Tile Base
            val isFocusedTile = isFocused && !gameOver && tiles.indexOf(tile) == focusedTileIdx
            val isSelected = tile == selectedTile
            
            paint.color = if (isSelected) neonCyan else if (isFocusedTile) Color.parseColor("#333333") else Color.parseColor("#222222")
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(left, top, right, bottom, 12f, 12f, paint)

            // Border
            paint.color = if (isSelected || isFocusedTile) neonCyan else Color.parseColor("#444444")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = if (isSelected || isFocusedTile) 4f else 2f
            canvas.drawRoundRect(left, top, right, bottom, 12f, 12f, paint)

            // Icon/ID
            paint.style = Paint.Style.FILL
            paint.color = textWhite
            paint.textSize = cellSize * 0.4f
            paint.textAlign = Paint.Align.CENTER
            val textOffset = (paint.descent() + paint.ascent()) / 2
            
            val scale = if (tile.id == lastMatchedId) matchAnimScale else 1.0f
            paint.textSize = cellSize * 0.4f * scale
            canvas.drawText(String.format(java.util.Locale.US, "%d", tile.id), left + (cellSize - 20) / 2, top + (cellSize - 20) / 2 - textOffset, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        val activeIndices = tiles.indices.filter { !tiles[it].matched }
        if (activeIndices.isEmpty()) return super.onKeyDown(keyCode, event)

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, 
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // Find next available tile index
                val currentIdx = activeIndices.indexOf(focusedTileIdx)
                val nextIdx = if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    (currentIdx + 1) % activeIndices.size
                } else {
                    (currentIdx - 1 + activeIndices.size) % activeIndices.size
                }
                focusedTileIdx = activeIndices[nextIdx]
                invalidate()
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                handleSelection(tiles[focusedTileIdx])
                return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }

    private fun handleSelection(tile: Tile) {
        if (!isTileFree(tile)) return

        if (selectedTile == null) {
            selectedTile = tile
        } else if (selectedTile == tile) {
            selectedTile = null
        } else if (selectedTile!!.id == tile.id) {
            selectedTile!!.matched = true
            tile.matched = true
            animateMatch(tile.id)
            selectedTile = null
            checkWin()
        } else {
            selectedTile = tile
        }
        invalidate()
    }

    private fun animateMatch(id: Int) {
        lastMatchedId = id
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, 1.5f, 0.0f)
        animator.duration = 300
        animator.addUpdateListener { 
            matchAnimScale = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver) {
            performClick()
            val cellSize = Math.min(width / 7f, height / 6f)
            val offsetX = (width - cellSize * 5) / 2
            val offsetY = (height - cellSize * 4) / 2

            val clickedTile = tiles.filter { !it.matched }.reversed().find { tile ->
                val left = offsetX + tile.x * cellSize + tile.z * 10f
                val top = offsetY + tile.y * cellSize - tile.z * 10f
                event.x in left..(left + cellSize - 20) && event.y in top..(top + cellSize - 20)
            }

            if (clickedTile != null) {
                handleSelection(clickedTile)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun isTileFree(tile: Tile): Boolean {
        val onTop = tiles.any { !it.matched && it.z == tile.z + 1 && Math.abs(it.x - tile.x) < 0.5 && Math.abs(it.y - tile.y) < 0.5 }
        if (onTop) return false
        
        val leftBlocked = tiles.any { !it.matched && it.z == tile.z && it.x == tile.x - 1 && it.y == tile.y }
        val rightBlocked = tiles.any { !it.matched && it.z == tile.z && it.x == tile.x + 1 && it.y == tile.y }
        
        return !leftBlocked || !rightBlocked
    }

    private fun checkWin() {
        if (tiles.all { it.matched }) {
            gameOver = true
            onWin?.invoke(1000)
        }
    }
}
