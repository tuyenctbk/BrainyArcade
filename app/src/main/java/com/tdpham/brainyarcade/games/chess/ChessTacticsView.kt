package com.tdpham.brainyarcade.games.chess

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

class ChessTacticsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val size = 8
    private val board = Array(size) { CharArray(size) { ' ' } }
    private var selectedX = 0
    private var selectedY = 0
    private var sourceX = -1
    private var sourceY = -1
    
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    
    // Animation
    private var animProgress = 1.0f
    private var animFromX = -1; private var animFromY = -1
    private var animToX = -1; private var animToY = -1
    private var animPiece = ' '

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0 until size) for (j in 0 until size) board[i][j] = ' '
        setupMateInOne()
        gameOver = false
        sourceX = -1
        sourceY = -1
        animProgress = 1.0f
        invalidate()
    }

    private fun setupMateInOne() {
        board[4][0] = 'k'
        board[4][7] = 'K'
        board[2][5] = 'B'
        board[5][2] = 'Q'
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / size
        val offsetX = (width - availableSize) / 2
        val offsetY = (height - availableSize) / 2

        // Draw Squares
        for (i in 0 until size) {
            for (j in 0 until size) {
                val left = offsetX + i * cellSize; val top = offsetY + j * cellSize
                paint.color = if ((i + j) % 2 == 0) Color.parseColor("#333333") else Color.parseColor("#222222")
                canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)

                if (i == selectedX && j == selectedY && isFocused) {
                    paint.color = neonCyan
                    paint.style = Paint.Style.STROKE; paint.strokeWidth = 6f
                    canvas.drawRect(left + 3, top + 3, left + cellSize - 3, top + cellSize - 3, paint)
                    paint.style = Paint.Style.FILL
                }
                if (i == sourceX && j == sourceY) {
                    paint.color = Color.parseColor("#6600BCD4")
                    canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)
                }

                val piece = board[i][j]
                if (piece != ' ' && !(i == animToX && j == animToY && animProgress < 1.0f)) {
                    drawPiece(canvas, piece, left + cellSize/2, top + cellSize/2, cellSize)
                }
            }
        }

        // Draw Animated Piece
        if (animProgress < 1.0f) {
            val drawX = offsetX + (animFromX + (animToX - animFromX) * animProgress) * cellSize + cellSize/2
            val drawY = offsetY + (animFromY + (animToY - animFromY) * animProgress) * cellSize + cellSize/2
            drawPiece(canvas, animPiece, drawX, drawY, cellSize)
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Char, cx: Float, cy: Float, cellSize: Float) {
        paint.color = if (piece.isUpperCase()) Color.WHITE else Color.parseColor("#AAAAAA")
        paint.textSize = cellSize * 0.7f
        paint.textAlign = Paint.Align.CENTER
        val textOffset = (paint.descent() + paint.ascent()) / 2
        canvas.drawText(piece.toString(), cx, cy - textOffset, paint)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || animProgress < 1.0f) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> { if (selectedY == 0) return false; selectedY = (selectedY - 1 + size) % size }
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedY = (selectedY + 1) % size
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedX = (selectedX - 1 + size) % size
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedX = (selectedX + 1) % size
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> handleSelection()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun handleSelection() {
        if (sourceX == -1) {
            if (board[selectedX][selectedY] != ' ' && board[selectedX][selectedY].isUpperCase()) {
                sourceX = selectedX; sourceY = selectedY
            }
        } else {
            if (sourceX != selectedX || sourceY != selectedY) {
                animateMove(sourceX, sourceY, selectedX, selectedY)
            } else {
                sourceX = -1; sourceY = -1
            }
        }
    }

    private fun animateMove(fx: Int, fy: Int, tx: Int, ty: Int) {
        animFromX = fx; animFromY = fy; animToX = tx; animToY = ty
        animPiece = board[fx][fy]
        board[fx][fy] = ' '
        
        val animator = android.animation.ValueAnimator.ofFloat(0.0f, 1.0f)
        animator.duration = 200
        animator.addUpdateListener { animProgress = it.animatedValue as Float; invalidate() }
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                board[tx][ty] = animPiece
                sourceX = -1; sourceY = -1
                checkMate()
            }
        })
        animator.start()
    }

    private fun checkMate() {
        if (board[5][1] == 'Q') {
            gameOver = true; onWin?.invoke(1000)
        } else if (board.all { row -> row.none { it == 'q' || it == 'Q' } }) {
            gameOver = true; onLose?.invoke()
        }
    }

    override fun onTouchEvent(event: MotionEvent) = super.onTouchEvent(event)
}
