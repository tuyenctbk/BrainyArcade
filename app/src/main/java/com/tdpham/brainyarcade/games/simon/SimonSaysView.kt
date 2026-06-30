package com.tdpham.brainyarcade.games.simon

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

class SimonSaysView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val colors = intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
    private val dimColors = intArrayOf(
        Color.parseColor("#44FF0000"), Color.parseColor("#4400FF00"),
        Color.parseColor("#440000FF"), Color.parseColor("#44FFFF00")
    )
    private val sequence = mutableListOf<Int>()
    private var userPos = 0
    private var isDisplaying = false
    private var activeColor = -1
    private var gameOver = false
    private var selectedIdx = 0
    var onGameOver: ((Int) -> Unit)? = null
    var onWin: ((Int) -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        sequence.clear()
        gameOver = false
        nextLevel()
    }

    private fun nextLevel() {
        sequence.add(Random.nextInt(4))
        userPos = 0
        displaySequence()
    }

    private fun displaySequence() {
        isDisplaying = true
        activeColor = -1
        Thread {
            for (colorIdx in sequence) {
                activeColor = colorIdx
                postInvalidate()
                try { Thread.sleep(600) } catch (e: Exception) {}
                activeColor = -1
                postInvalidate()
                try { Thread.sleep(200) } catch (e: Exception) {}
            }
            isDisplaying = false
            postInvalidate()
        }.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(width, height) / 2.5f

        for (i in 0..3) {
            paint.color = if (activeColor == i) colors[i] else dimColors[i]
            paint.style = Paint.Style.FILL
            val startAngle = i * 90f
            canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 
                startAngle, 90f, true, paint)
            
            if (i == selectedIdx && isFocused && !isDisplaying && !gameOver) {
                paint.color = neonCyan
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = context.resources.getDimension(R.dimen.game_focus_border_width) * 2
                canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 
                    startAngle, 90f, true, paint)
            }
        }

        if (gameOver) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(context.getString(R.string.victory_msg, (sequence.size - 1).toString()), width / 2f, height / 2f, paint)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver || isDisplaying) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (selectedIdx == 0) return false
                selectedIdx = 0
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedIdx = 1
            KeyEvent.KEYCODE_DPAD_DOWN -> selectedIdx = 2
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedIdx = 3
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> handleInput(selectedIdx)
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun handleInput(idx: Int) {
        if (idx == sequence[userPos]) {
            userPos++
            if (userPos == sequence.size) {
                if (sequence.size >= 10) {
                    gameOver = true
                    onWin?.invoke(sequence.size)
                } else {
                    nextLevel()
                }
            }
        } else {
            gameOver = true
            onGameOver?.invoke(sequence.size - 1)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !gameOver && !isDisplaying) {
            performClick()
            // Touch logic for arcs can be complex, skipping for brevity in basic D-pad fix
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
