package com.tdpham.brainyarcade.games.mastermind

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

class MastermindView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val maxGuesses = 10
    private val codeLength = 4
    private val colorValues = intArrayOf(
        Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, 
        Color.parseColor("#FFA500"), Color.parseColor("#800080")
    )

    private val guesses = Array(maxGuesses) { IntArray(codeLength) { -1 } }
    private val clues = Array(maxGuesses) { IntArray(2) { 0 } }
    private val secretCode = IntArray(codeLength)
    private var currentGuessRow = 0
    private var selectedSlot = 0
    private var gameOver = false
    private var pegPopScale = 1.0f
    private var lastPegX = -1
    private var lastPegY = -1
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private val grayDark = ContextCompat.getColor(context, R.color.text_gray_dark)

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) kotlin.random.Random.Default else kotlin.random.Random(seed)
        for (i in 0 until codeLength) {
            secretCode[i] = rand.nextInt(colorValues.size)
        }
        currentGuessRow = 0
        selectedSlot = 0
        gameOver = false
        for (i in 0 until maxGuesses) {
            for (j in 0 until codeLength) guesses[i][j] = -1
            clues[i][0] = 0
            clues[i][1] = 0
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableHeight = height.toFloat()
        val availableWidth = width.toFloat()
        val verticalPadding = availableHeight * 0.1f
        val rowHeight = (availableHeight - verticalPadding * 2) / maxGuesses
        val slotWidth = availableWidth / (codeLength + 3)

        for (r in 0 until maxGuesses) {
            val y = availableHeight - verticalPadding - (r + 0.5f) * rowHeight
            
            for (s in 0 until codeLength) {
                val x = (s + 1) * slotWidth
                val colorIdx = guesses[r][s]
                
                paint.color = ContextCompat.getColor(context, R.color.background_pitch_black)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(x, y, rowHeight * 0.35f, paint)

                val scale = if (r == lastPegY && s == lastPegX) pegPopScale else 1.0f
                paint.color = if (colorIdx == -1) grayDark else colorValues[colorIdx]
                canvas.drawCircle(x, y, rowHeight * 0.3f * scale, paint)

                if (r == currentGuessRow && s == selectedSlot && isFocused && !gameOver) {
                    paint.color = neonCyan
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = context.resources.getDimension(R.dimen.game_inner_focus_border_width) * 2
                    canvas.drawCircle(x, y, rowHeight * 0.4f, paint)
                }
            }

            val clueX = (codeLength + 1.5f) * slotWidth
            drawClue(canvas, clueX, y, clues[r][0], clues[r][1], rowHeight * 0.12f)
        }
    }

    private fun drawClue(canvas: Canvas, x: Float, y: Float, black: Int, white: Int, radius: Float) {
        var count = 0
        paint.style = Paint.Style.FILL
        for (i in 0 until black) {
            paint.color = Color.WHITE
            val dx = if (count % 2 == 0) -radius * 1.5f else radius * 1.5f
            val dy = if (count < 2) -radius * 1.5f else radius * 1.5f
            canvas.drawCircle(x + dx, y + dy, radius, paint)
            count++
        }
        for (i in 0 until white) {
            paint.color = Color.GRAY
            val dx = if (count % 2 == 0) -radius * 1.5f else radius * 1.5f
            val dy = if (count < 2) -radius * 1.5f else radius * 1.5f
            canvas.drawCircle(x + dx, y + dy, radius, paint)
            count++
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                // In Mastermind, we use UP to change color.
                // To allow escape, maybe if they press UP but color is already changed?
                // Let's just always use Menu for help in Mastermind if it's too cramped.
                // OR, if they are at the top and press UP? But currentGuessRow starts at 0 and goes UP?
                // Actually availableHeight - (r+1)*rowHeight means r=maxGuesses-1 is top?
                // Let's just allow escape if r == currentGuessRow and we specifically want to go up.
                // Actually MastermindView is full screen.
                changeColor(1)
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> changeColor(-1)
            KeyEvent.KEYCODE_DPAD_LEFT -> selectedSlot = (selectedSlot - 1 + codeLength) % codeLength
            KeyEvent.KEYCODE_DPAD_RIGHT -> selectedSlot = (selectedSlot + 1) % codeLength
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> submitGuess()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun changeColor(delta: Int) {
        val current = if (guesses[currentGuessRow][selectedSlot] == -1) 0 else guesses[currentGuessRow][selectedSlot]
        guesses[currentGuessRow][selectedSlot] = (current + delta + colorValues.size) % colorValues.size
        animatePegPop(selectedSlot, currentGuessRow)
    }

    private fun animatePegPop(x: Int, y: Int) {
        lastPegX = x
        lastPegY = y
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, 1.4f, 1.0f)
        animator.duration = 150
        animator.addUpdateListener { 
            pegPopScale = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun submitGuess() {
        if (guesses[currentGuessRow].any { it == -1 }) return
        val black = calculateBlackPegs(guesses[currentGuessRow], secretCode)
        val totalMatch = calculateWhitePegs(guesses[currentGuessRow], secretCode)
        val white = totalMatch - black
        clues[currentGuessRow][0] = black
        clues[currentGuessRow][1] = white
        if (black == codeLength) {
            gameOver = true
            onWin?.invoke(1000)
        } else if (++currentGuessRow >= maxGuesses) {
            gameOver = true
            onLose?.invoke()
        }
    }

    private fun calculateBlackPegs(guess: IntArray, secret: IntArray) = guess.indices.count { guess[it] == secret[it] }

    private fun calculateWhitePegs(guess: IntArray, secret: IntArray): Int {
        val gCount = IntArray(colorValues.size); val sCount = IntArray(colorValues.size)
        for (i in guess.indices) { gCount[guess[i]]++; sCount[secret[i]]++ }
        return colorValues.indices.sumOf { Math.min(gCount[it], sCount[it]) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
