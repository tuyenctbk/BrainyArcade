package com.tdpham.brainyarcade.games.sudoku

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.CycleInterpolator
import androidx.core.content.ContextCompat
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.infra.GameView
import java.util.Stack

class SudokuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 9
    private var board = Array(size) { IntArray(size) }
    private var isFixed = Array(size) { BooleanArray(size) }
    private var selectedX = 0
    private var selectedY = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lastPlacedX = -1
    private var lastPlacedY = -1
    private var cellPopValue = 0f
    
    private val history = Stack<Array<IntArray>>()

    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val neonCyanTrans = ContextCompat.getColor(context, R.color.neon_cyan_transparent)
    private val textWhite = ContextCompat.getColor(context, R.color.text_white)
    private val grayDark = ContextCompat.getColor(context, R.color.text_gray_dark)

    private val linePaint = Paint().apply {
        color = grayDark
        strokeWidth = 2f
    }
    
    private val thickLinePaint = Paint().apply {
        color = textWhite
        strokeWidth = 6f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = neonCyan
        textAlign = Paint.Align.CENTER
    }

    private val fixedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textWhite
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 0f, Color.BLACK)
    }

    private val selectionPaint = Paint().apply {
        color = neonCyan
        style = Paint.Style.STROKE
        setShadowLayer(10f, 0f, 0f, neonCyan)
    }

    private val selectionBgPaint = Paint().apply {
        color = Color.parseColor("#3300BCD4")
        style = Paint.Style.FILL
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
        
        selectionPaint.strokeWidth = context.resources.getDimension(R.dimen.game_focus_border_width)
    }

    override fun resetGame(seed: Long, level: Int) {
        val generator = SudokuGenerator(seed)
        // Difficulty: levels 1-100 -> removal count 30-60
        val removalCount = 30 + Math.min(level / 3, 30)
        board = generator.generate(removalCount)
        for (i in 0 until size) {
            for (j in 0 until size) {
                isFixed[i][j] = board[i][j] != 0
            }
        }
        history.clear()
        gameOver = false
        invalidate()
    }

    override fun undo() {
        if (history.isNotEmpty()) {
            val prev = history.pop()
            for (i in 0 until size) board[i] = prev[i].copyOf()
            invalidate()
        }
    }

    private fun saveHistory() {
        val snapshot = Array(size) { i -> board[i].copyOf() }
        history.push(snapshot)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSide = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        if (isFocused) {
            canvas.drawRect(
                offsetX + selectedX * cellSide,
                offsetY + selectedY * cellSide,
                offsetX + (selectedX + 1) * cellSide,
                offsetY + (selectedY + 1) * cellSide,
                selectionBgPaint
            )
        }

        paint.color = neonCyan
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.setShadowLayer(15f, 0f, 0f, neonCyan)
        canvas.drawRect(offsetX, offsetY, offsetX + availableSize, offsetY + availableSize, paint)
        paint.clearShadowLayer()

        for (i in 1 until size) {
            val p = if (i % 3 == 0) thickLinePaint else linePaint
            canvas.drawLine(offsetX + i * cellSide, offsetY, offsetX + i * cellSide, offsetY + availableSize, p)
            canvas.drawLine(offsetX, offsetY + i * cellSide, offsetX + availableSize, offsetY + i * cellSide, p)
        }

        if (isFocused) {
            val inset = selectionPaint.strokeWidth / 2
            canvas.drawRect(
                offsetX + selectedX * cellSide + inset,
                offsetY + selectedY * cellSide + inset,
                offsetX + (selectedX + 1) * cellSide - inset,
                offsetY + (selectedY + 1) * cellSide - inset,
                selectionPaint
            )
        }

        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j] != 0) {
                    val p = if (isFixed[i][j]) fixedTextPaint else textPaint
                    val scale = if (i == lastPlacedX && j == lastPlacedY) cellPopValue else 1.0f
                    p.textSize = cellSide * 0.6f * scale
                    canvas.drawText(
                        String.format(java.util.Locale.US, "%d", board[i][j]),
                        offsetX + i * cellSide + cellSide / 2,
                        offsetY + j * cellSide + cellSide / 2 - (p.descent() + p.ascent()) / 2,
                        p
                    )
                }
            }
        }

        if (gameOver) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 80f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("VICTORY!", width / 2f, height / 2f, paint)
        }
    }

    var onWin: ((Int) -> Unit)? = null

    private fun checkWin() {
        if (board.all { row -> row.all { it != 0 } }) {
            gameOver = true
            onWin?.invoke(100)
        }
    }

    private var gameOver = false

    private fun shake() {
        this.animate()
            .translationX(10f)
            .setDuration(300)
            .setInterpolator(CycleInterpolator(4f))
            .start()
    }

    private fun animateCellPop(x: Int, y: Int) {
        lastPlacedX = x
        lastPlacedY = y
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, 1.4f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { 
            cellPopValue = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (selectedY == 0) return false
                selectedY = (selectedY - 1 + size) % size
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            in KeyEvent.KEYCODE_1..KeyEvent.KEYCODE_9 -> {
                if (!isFixed[selectedX][selectedY]) {
                    saveHistory()
                    board[selectedX][selectedY] = keyCode - KeyEvent.KEYCODE_0
                    animateCellPop(selectedX, selectedY)
                    checkWin()
                } else {
                    shake()
                }
            }
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_0 -> {
                if (!isFixed[selectedX][selectedY]) {
                    saveHistory()
                    board[selectedX][selectedY] = 0
                }
            }
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val availableSize = Math.min(width, height).toFloat()
            val cellSide = availableSize / size
            val offsetX = (width - availableSize) / 2
            val offsetY = (height - availableSize) / 2
            val x = ((event.x - offsetX) / cellSide).toInt()
            val y = ((event.y - offsetY) / cellSide).toInt()
            if (x in 0 until size && y in 0 until size) {
                selectedX = x; selectedY = y
                invalidate()
                requestFocus()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
