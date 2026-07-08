package com.tdpham.brainyarcade.games.hanoi

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

class HanoiView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val numDisks = 5
    private val pegs = Array(3) { Stack<Int>() }
    private var selectedPeg = 0
    private var sourcePeg = -1
    private var gameOver = false
    var onWin: ((Int) -> Unit)? = null
    var onMove: ((Int) -> Unit)? = null
    private var moves = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val neonCyanTrans = ContextCompat.getColor(context, R.color.neon_cyan_transparent)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        for (i in 0..2) pegs[i].clear()
        for (i in numDisks downTo 1) pegs[0].push(i)
        gameOver = false
        selectedPeg = 0
        sourcePeg = -1
        moves = 0
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pegWidth = width / 3f
        val baseLine = height * 0.8f
        val diskHeight = 40f

        paint.color = ContextCompat.getColor(context, R.color.text_gray_dark)
        canvas.drawRect(50f, baseLine, width - 50f, baseLine + 20f, paint)

        for (i in 0..2) {
            val pegX = i * pegWidth + pegWidth / 2
            
            paint.color = ContextCompat.getColor(context, R.color.text_gray_light)
            canvas.drawRect(pegX - 8f, baseLine - numDisks * diskHeight - 60f, pegX + 8f, baseLine, paint)

            if ((i == selectedPeg && isFocused) || i == sourcePeg) {
                paint.color = neonCyanTrans
                canvas.drawRect(i * pegWidth + 10, 20f, (i + 1) * pegWidth - 10, baseLine + 10, paint)
                
                if (i == selectedPeg && isFocused) {
                    paint.color = neonCyan
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 4f
                    paint.setShadowLayer(10f, 0f, 0f, neonCyan)
                    canvas.drawRoundRect(i * pegWidth + 10, 20f, (i + 1) * pegWidth - 10, baseLine + 10, 12f, 12f, paint)
                    paint.clearShadowLayer()
                    paint.style = Paint.Style.FILL
                }
            }

            for (idx in 0 until pegs[i].size) {
                val diskSize = pegs[i][idx]
                val diskWidth = (diskSize / numDisks.toFloat()) * (pegWidth * 0.8f)
                val top = baseLine - (idx + 1) * diskHeight
                
                paint.color = getDiskColor(diskSize)
                canvas.drawRoundRect(pegX - diskWidth / 2, top, pegX + diskWidth / 2, top + diskHeight - 5f, 8f, 8f, paint)
            }
        }
    }

    private fun getDiskColor(size: Int): Int {
        val colors = intArrayOf(
            Color.parseColor("#E50914"), Color.parseColor("#00BCD4"), 
            Color.parseColor("#FFD700"), Color.parseColor("#32CD32"), 
            Color.parseColor("#9370DB")
        )
        return colors[(size - 1) % colors.size]
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> return false
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedPeg = (selectedPeg - 1 + 3) % 3
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedPeg = (selectedPeg + 1) % 3
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> handleSelection()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun handleSelection() {
        if (sourcePeg == -1) {
            if (pegs[selectedPeg].isNotEmpty()) {
                sourcePeg = selectedPeg
            }
        } else {
            if (sourcePeg != selectedPeg) {
                val disk = pegs[sourcePeg].peek()
                if (pegs[selectedPeg].isEmpty() || pegs[selectedPeg].peek() > disk) {
                    pegs[selectedPeg].push(pegs[sourcePeg].pop())
                    moves++
                    onMove?.invoke(moves)
                    checkWin()
                }
            }
            sourcePeg = -1
        }
    }

    private fun checkWin() {
        if (pegs[2].size == numDisks) {
            gameOver = true
            onWin?.invoke(moves)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val pegWidth = width / 3f
            if (pegWidth > 0) {
                val pegIdx = (event.x / pegWidth).toInt().coerceIn(0, 2)
                selectedPeg = pegIdx
                handleSelection()
                invalidate()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
