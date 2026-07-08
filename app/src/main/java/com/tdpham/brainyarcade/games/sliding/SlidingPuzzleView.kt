package com.tdpham.brainyarcade.games.sliding

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

class SlidingPuzzleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 4
    private val board = IntArray(size * size)
    private var emptyPos = size * size - 1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onMove: ((Int) -> Unit)? = null
    private var moves = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val bgDeep = ContextCompat.getColor(context, R.color.background_deep_gray)

    private var animProgress = 1.0f
    private var lastEmptyPos = -1
    private var lastSwappedPos = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random.Default else Random(seed)
        for (i in 0 until size * size) board[i] = i + 1
        board[size * size - 1] = 0
        emptyPos = size * size - 1
        
        // Solvable shuffle
        repeat(100) {
            val neighbors = getNeighbors(emptyPos)
            val move = neighbors[rand.nextInt(neighbors.size)]
            swap(emptyPos, move)
            emptyPos = move
        }
        
        gameOver = false
        moves = 0
        animProgress = 1.0f
        invalidate()
    }

    private fun getNeighbors(pos: Int): List<Int> {
        val n = mutableListOf<Int>()
        val r = pos / size; val c = pos % size
        if (r > 0) n.add(pos - size)
        if (r < size - 1) n.add(pos + size)
        if (c > 0) n.add(pos - 1)
        if (c < size - 1) n.add(pos + 1)
        return n
    }

    private fun swap(p1: Int, p2: Int) {
        val temp = board[p1]; board[p1] = board[p2]; board[p2] = temp
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        paint.color = bgDeep
        canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)

        for (i in 0 until size * size) {
            val value = board[i]
            if (value == 0) continue

            var r = i / size; var c = i % size
            var drawX = offsetX + c * cellSize
            var drawY = offsetY + r * cellSize

            if (i == lastEmptyPos && animProgress < 1.0f) {
                val oldR = lastSwappedPos / size; val oldC = lastSwappedPos % size
                val newR = lastEmptyPos / size; val newC = lastEmptyPos % size
                drawX = offsetX + (oldC + (newC - oldC) * animProgress) * cellSize
                drawY = offsetY + (oldR + (newR - oldR) * animProgress) * cellSize
            }

            val left = drawX + 6; val top = drawY + 6
            val right = drawX + cellSize - 6; val bottom = drawY + cellSize - 6

            paint.color = Color.parseColor("#333333")
            canvas.drawRoundRect(left, top, right, bottom, 12f, 12f, paint)

            paint.color = Color.WHITE
            paint.textSize = cellSize * 0.4f
            paint.textAlign = Paint.Align.CENTER
            val textOffset = (paint.descent() + paint.ascent()) / 2
            canvas.drawText(String.format(java.util.Locale.US, "%d", value), drawX + cellSize / 2, drawY + cellSize / 2 - textOffset, paint)
        }

        if (isFocused && !gameOver) {
            paint.color = neonCyan; paint.style = Paint.Style.STROKE; paint.strokeWidth = 6f
            canvas.drawRoundRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, 12f, 12f, paint)
            paint.style = Paint.Style.FILL
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || animProgress < 1.0f) return super.onKeyDown(keyCode, event)
        val r = emptyPos / size; val c = emptyPos % size
        var target = -1
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (r == size - 1) return false
                target = emptyPos + size
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> if (r > 0) target = emptyPos - size
            KeyEvent.KEYCODE_DPAD_LEFT -> if (c < size - 1) target = emptyPos + 1
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (c > 0) target = emptyPos - 1
            else -> return super.onKeyDown(keyCode, event)
        }

        if (target != -1) {
            animateSlide(emptyPos, target)
            swap(emptyPos, target)
            emptyPos = target
            moves++; onMove?.invoke(moves); checkWin()
        }
        return true
    }

    private fun animateSlide(empty: Int, target: Int) {
        lastEmptyPos = empty
        lastSwappedPos = target
        val animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 150
        animator.addUpdateListener { animProgress = it.animatedValue as Float; invalidate() }
        animator.start()
    }

    private fun checkWin() {
        if ((0 until size * size - 1).all { board[it] == it + 1 }) {
            gameOver = true; onWin?.invoke(moves)
        }
    }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (gameOver || animProgress < 1.0f) return false
            val availableSize = Math.min(width, height).toFloat()
            val cellSize = availableSize / size
            val offsetX = (width - availableSize) / 2
            val offsetY = (height - availableSize) / 2

            val x = e.x
            val y = e.y

            if (x >= offsetX && x < offsetX + availableSize && y >= offsetY && y < offsetY + availableSize) {
                val c = ((x - offsetX) / cellSize).toInt()
                val r = ((y - offsetY) / cellSize).toInt()
                val tappedPos = r * size + c
                
                val neighbors = getNeighbors(emptyPos)
                if (neighbors.contains(tappedPos)) {
                    animateSlide(emptyPos, tappedPos)
                    swap(emptyPos, tappedPos)
                    emptyPos = tappedPos
                    moves++
                    onMove?.invoke(moves)
                    checkWin()
                    return true
                }
            }
            return false
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
