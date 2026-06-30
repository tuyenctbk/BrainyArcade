package com.tdpham.brainyarcade.games.tangram

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.tdpham.brainyarcade.infra.GameView

class TangramView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    data class Piece(val path: Path, var x: Float, var y: Float, var angle: Float, val color: Int)

    private val pieces = mutableListOf<Piece>()
    private var selectedPieceIdx = -1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        pieces.clear()
        val size = 200f
        
        pieces.add(Piece(createTriangle(size * 2, size), 100f, 100f, 0f, Color.RED))
        pieces.add(Piece(createTriangle(size * 2, size), 300f, 100f, 90f, Color.BLUE))
        pieces.add(Piece(createTriangle(size * 1.414f, size * 0.707f), 100f, 300f, 45f, Color.GREEN))
        pieces.add(Piece(createTriangle(size, size / 2), 250f, 300f, 0f, Color.YELLOW))
        pieces.add(Piece(createTriangle(size, size / 2), 350f, 300f, 180f, Color.MAGENTA))
        pieces.add(Piece(createSquare(size * 0.707f), 100f, 450f, 0f, Color.CYAN))
        pieces.add(Piece(createParallelogram(size, size / 2), 300f, 450f, 0f, Color.GRAY))

        gameOver = false
        selectedPieceIdx = -1
        invalidate()
    }

    private fun createTriangle(base: Float, height: Float): Path {
        return Path().apply {
            moveTo(0f, 0f)
            lineTo(base, 0f)
            lineTo(base / 2, height)
            close()
        }
    }

    private fun createSquare(side: Float): Path {
        return Path().apply {
            addRect(0f, 0f, side, side, Path.Direction.CW)
        }
    }

    private fun createParallelogram(base: Float, height: Float): Path {
        return Path().apply {
            moveTo(0f, 0f)
            lineTo(base, 0f)
            lineTo(base + height / 2, height)
            lineTo(height / 2, height)
            close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.parseColor("#333333")
        canvas.drawRect(width / 2f - 150f, height / 2f - 150f, width / 2f + 150f, height / 2f + 150f, paint)

        for (i in pieces.indices) {
            val piece = pieces[i]
            canvas.save()
            canvas.translate(piece.x, piece.y)
            canvas.rotate(piece.angle)
            
            paint.color = piece.color
            paint.style = Paint.Style.FILL
            canvas.drawPath(piece.path, paint)
            
            if (i == selectedPieceIdx && isFocused) {
                paint.color = Color.parseColor("#00BCD4")
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f
                canvas.drawPath(piece.path, paint)
            }
            canvas.restore()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                selectedPieceIdx = (selectedPieceIdx + 1) % pieces.size
            }
            KeyEvent.KEYCODE_DPAD_UP -> if (selectedPieceIdx != -1) pieces[selectedPieceIdx].y -= 10
            KeyEvent.KEYCODE_DPAD_DOWN -> if (selectedPieceIdx != -1) pieces[selectedPieceIdx].y += 10
            KeyEvent.KEYCODE_DPAD_LEFT -> if (selectedPieceIdx != -1) pieces[selectedPieceIdx].x -= 10
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (selectedPieceIdx != -1) pieces[selectedPieceIdx].x += 10
            KeyEvent.KEYCODE_R -> if (selectedPieceIdx != -1) pieces[selectedPieceIdx].angle = (pieces[selectedPieceIdx].angle + 45) % 360
            KeyEvent.KEYCODE_SPACE -> checkWin()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun checkWin() {
        // Simplified win: user presses space and we assume they solved it for this skeleton
        gameOver = true
        onWin?.invoke(1200)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }
}
