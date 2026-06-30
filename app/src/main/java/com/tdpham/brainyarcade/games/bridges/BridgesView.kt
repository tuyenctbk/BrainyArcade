package com.tdpham.brainyarcade.games.bridges

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

class BridgesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 6
    private val islands = mutableListOf<Island>()
    private val bridges = mutableListOf<Bridge>()
    
    private var selectedIslandIdx = -1
    private var sourceIslandIdx = -1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val textWhite = ContextCompat.getColor(context, R.color.text_white)
    
    private var animProgress = 1.0f
    private var animBridge: Bridge? = null

    data class Island(val x: Int, val y: Int, val target: Int, var current: Int = 0)
    data class Bridge(val i1: Int, val i2: Int, var count: Int = 1)

    private val levelPack = listOf(
        listOf(Island(1, 1, 2), Island(4, 1, 2), Island(1, 4, 2), Island(4, 4, 2)),
        listOf(Island(0, 0, 3), Island(3, 0, 2), Island(5, 0, 3), Island(0, 3, 2), Island(5, 3, 2), Island(0, 5, 3), Island(3, 5, 2), Island(5, 5, 3)),
        listOf(Island(1, 0, 4), Island(4, 0, 4), Island(0, 2, 2), Island(5, 2, 2), Island(1, 5, 4), Island(4, 5, 4)),
        listOf(Island(0, 0, 2), Island(2, 0, 3), Island(5, 0, 2), Island(2, 2, 8), Island(0, 5, 2), Island(2, 5, 3), Island(5, 5, 2)),
        listOf(Island(0, 1, 2), Island(2, 1, 4), Island(4, 1, 2), Island(0, 4, 2), Island(2, 4, 4), Island(4, 4, 2))
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random() else Random(seed)
        val levelData = levelPack[rand.nextInt(levelPack.size)]
        
        islands.clear()
        bridges.clear()
        for (isl in levelData) islands.add(isl.copy(current = 0))
        
        selectedIslandIdx = 0
        sourceIslandIdx = -1
        gameOver = false
        animProgress = 1.0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        // Draw Bridges
        paint.color = neonCyan; paint.strokeWidth = 5f
        for (b in bridges) {
            val s = islands[b.i1]; val e = islands[b.i2]
            val sx = offsetX + s.x * cellSize + cellSize / 2
            val sy = offsetY + s.y * cellSize + cellSize / 2
            val ex = offsetX + e.x * cellSize + cellSize / 2
            val ey = offsetY + e.y * cellSize + cellSize / 2
            
            val progress = if (b == animBridge) animProgress else 1.0f
            val curEx = sx + (ex - sx) * progress
            val curEy = sy + (ey - sy) * progress

            if (b.count == 1) {
                canvas.drawLine(sx, sy, curEx, curEy, paint)
            } else if (b.count == 2) {
                val dx = if (sy == ey) 0f else 12f
                val dy = if (sx == ex) 0f else 12f
                canvas.drawLine(sx - dx, sy - dy, sx + (ex - sx) * progress - dx, sy + (ey - sy) * progress - dy, paint)
                canvas.drawLine(sx + dx, sy + dy, sx + (ex - sx) * progress + dx, sy + (ey - sy) * progress + dy, paint)
            }
        }

        // Draw Islands
        for (idx in islands.indices) {
            val island = islands[idx]
            val cx = offsetX + island.x * cellSize + cellSize / 2
            val cy = offsetY + island.y * cellSize + cellSize / 2

            paint.style = Paint.Style.FILL
            paint.color = if (island.current == island.target) Color.parseColor("#388E3C") else Color.parseColor("#333333")
            canvas.drawCircle(cx, cy, cellSize * 0.35f, paint)

            if (idx == selectedIslandIdx && isFocused) {
                paint.style = Paint.Style.STROKE; paint.color = Color.WHITE; paint.strokeWidth = 5f
                canvas.drawCircle(cx, cy, cellSize * 0.42f, paint)
                paint.style = Paint.Style.FILL
            }
            if (idx == sourceIslandIdx) {
                paint.style = Paint.Style.STROKE; paint.color = neonCyan; paint.strokeWidth = 6f
                canvas.drawCircle(cx, cy, cellSize * 0.45f, paint)
                paint.style = Paint.Style.FILL
            }

            paint.color = textWhite; paint.textSize = cellSize * 0.45f; paint.textAlign = Paint.Align.CENTER
            val textOffset = (paint.descent() + paint.ascent()) / 2
            canvas.drawText(String.format(java.util.Locale.US, "%d", island.target), cx, cy - textOffset, paint)
        }
        
        if (gameOver) {
            paint.style = Paint.Style.FILL; paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE; paint.textSize = 60f; paint.textAlign = Paint.Align.CENTER
            canvas.drawText("VICTORY!", width/2f, height/2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || animProgress < 1.0f) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                val s = islands[selectedIslandIdx]
                if (s.y == 0 && sourceIslandIdx == -1) return false
                moveSelection(0, -1)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> moveSelection(0, 1)
            KeyEvent.KEYCODE_DPAD_LEFT -> moveSelection(-1, 0)
            KeyEvent.KEYCODE_DPAD_RIGHT -> moveSelection(1, 0)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> handleSelection()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun moveSelection(dx: Int, dy: Int) {
        val s = islands[selectedIslandIdx]
        var bestIdx = -1; var minDist = 100
        for (i in islands.indices) {
            if (i == selectedIslandIdx) continue
            val island = islands[i]
            val dist = Math.abs(island.x - s.x) + Math.abs(island.y - s.y)
            if (dx > 0 && island.x > s.x && island.y == s.y && dist < minDist) { minDist = dist; bestIdx = i }
            if (dx < 0 && island.x < s.x && island.y == s.y && dist < minDist) { minDist = dist; bestIdx = i }
            if (dy > 0 && island.y > s.y && island.x == s.x && dist < minDist) { minDist = dist; bestIdx = i }
            if (dy < 0 && island.y < s.y && island.x == s.x && dist < minDist) { minDist = dist; bestIdx = i }
        }
        if (bestIdx != -1) selectedIslandIdx = bestIdx
    }

    private fun handleSelection() {
        if (sourceIslandIdx == -1) {
            sourceIslandIdx = selectedIslandIdx
        } else {
            if (sourceIslandIdx != selectedIslandIdx) {
                val s = islands[sourceIslandIdx]; val e = islands[selectedIslandIdx]
                if (s.x == e.x || s.y == e.y) {
                    if (!isBlocked(s, e)) toggleBridge(sourceIslandIdx, selectedIslandIdx)
                }
            }
            sourceIslandIdx = -1
        }
    }

    private fun isBlocked(s: Island, e: Island): Boolean {
        for (b in bridges) {
            val i1 = islands[b.i1]; val i2 = islands[b.i2]
            if (intersects(s, e, i1, i2)) return true
        }
        return false
    }

    private fun intersects(s1: Island, e1: Island, s2: Island, e2: Island): Boolean {
        // If they share an island, they don't intersect (crossing)
        if (s1 == s2 || s1 == e2 || e1 == s2 || e1 == e2) return false
        
        val h1 = s1.y == e1.y; val h2 = s2.y == e2.y
        if (h1 == h2) return false // Parallel
        
        val hor = if (h1) s1 to e1 else s2 to e2
        val ver = if (!h1) s1 to e1 else s2 to e2
        
        val xMin = Math.min(hor.first.x, hor.second.x); val xMax = Math.max(hor.first.x, hor.second.x)
        val yMin = Math.min(ver.first.y, ver.second.y); val yMax = Math.max(ver.first.y, ver.second.y)
        
        return ver.first.x in (xMin + 1) until xMax && hor.first.y in (yMin + 1) until yMax
    }

    private fun toggleBridge(i1: Int, i2: Int) {
        val existing = bridges.find { (it.i1 == i1 && it.i2 == i2) || (it.i1 == i2 && it.i2 == i1) }
        if (existing != null) {
            if (existing.count == 1) {
                existing.count = 2; islands[i1].current++; islands[i2].current++
                animateBridgeDraw(existing)
            } else {
                islands[i1].current -= 2; islands[i2].current -= 2
                bridges.remove(existing)
            }
        } else {
            val nb = Bridge(i1, i2, 1)
            bridges.add(nb); islands[i1].current++; islands[i2].current++
            animateBridgeDraw(nb)
        }
        checkWin()
    }

    private fun animateBridgeDraw(b: Bridge) {
        animBridge = b
        val animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { animProgress = it.animatedValue as Float; invalidate() }
        animator.start()
    }

    private fun checkWin() {
        if (islands.all { it.current == it.target }) {
            gameOver = true; onWin?.invoke(1000)
        }
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
