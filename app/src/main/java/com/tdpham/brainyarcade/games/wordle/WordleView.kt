package com.tdpham.brainyarcade.games.wordle

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

class WordleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), GameView {

    private val maxAttempts = 6
    private val wordLength = 5
    private val wordList = listOf(
        "BRAIN", "LOGIC", "THINK", "SMART", "LEVEL", "SOLVE", "INPUT", "TOUCH", "GAMES", "WORDS",
        "FRAME", "DREAM", "POINT", "SPACE", "START", "LIGHT", "MATCH", "LEARN", "STUDY", "QUEST",
        "FOUND", "FIELD", "BUILD", "CRAFT", "SKILL", "SPEED", "TOTAL", "BASIC", "CLEAN", "CLEAR",
        "FRESH", "GREEN", "POWER", "FORCE", "SOUND", "VOICE", "IMAGE", "VIDEO", "PHOTO", "MUSIC",
        "EARTH", "WATER", "FLAME", "STONE", "NIGHT", "NATURE", "CLOUD", "STORM", "BREAD", "TABLE",
        "CHAIR", "PLANT", "CLOCK", "PAPER", "GLASS", "CLOCK", "RADIO", "STAIR", "DOOR", "SPACE",
        "HEART", "SMILE", "SIGHT", "GHOST", "GHOST", "BRAVE", "BOLD", "QUICK", "QUITE", "STILL",
        "LUCKY", "PRIME", "GREAT", "GRAND", "SHARP", "SHINE", "SHORE", "SHIRT", "SWEET", "SWIFT"
    )
    private var targetWord = ""
    private val guesses = Array(maxAttempts) { "" }
    private var currentAttempt = 0
    private var gameOver = false
    private var win = false
    var onWin: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val neonCyan = ContextCompat.getColor(context, R.color.neon_cyan)
    private var letterPopScale = 1.0f
    private var lastPopX = -1
    private var lastPopY = -1

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        resetGame()
    }

    override fun resetGame(seed: Long, level: Int) {
        val rand = if (seed == -1L) Random.Default else Random(seed)
        targetWord = wordList[rand.nextInt(wordList.size)]
        guesses.fill("")
        currentAttempt = 0
        gameOver = false
        win = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val availableSize = Math.min(width, height).toFloat()
        val cellSize = availableSize / (maxAttempts + 1)
        val offsetX = (width - cellSize * wordLength) / 2
        val offsetY = (height - cellSize * maxAttempts) / 2

        for (r in 0 until maxAttempts) {
            val guess = guesses[r]
            for (c in 0 until wordLength) {
                val left = offsetX + c * cellSize + 8
                val top = offsetY + r * cellSize + 8
                val right = left + cellSize - 16
                val bottom = top + cellSize - 16

                if (r < currentAttempt || (gameOver && r == currentAttempt)) {
                    val char = if (c < guess.length) guess[c] else ' '
                    paint.color = getStatusColor(char, c, r)
                    paint.style = Paint.Style.FILL
                    canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paint)
                } else {
                    paint.color = Color.parseColor("#333333")
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f
                    canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, paint)
                }

                if (c < guess.length) {
                    val scale = if (r == lastPopY && c == lastPopX) letterPopScale else 1.0f
                    paint.color = Color.WHITE
                    paint.style = Paint.Style.FILL
                    paint.textSize = cellSize * 0.5f * scale
                    paint.textAlign = Paint.Align.CENTER
                    val textOffset = (paint.descent() + paint.ascent()) / 2
                    canvas.drawText(String.format(java.util.Locale.US, "%c", guess[c]), left + (cellSize - 16) / 2, top + (cellSize - 16) / 2 - textOffset, paint)
                }

                if (r == currentAttempt && !gameOver && isFocused) {
                    paint.color = neonCyan
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 4f
                    paint.setShadowLayer(10f, 0f, 0f, neonCyan)
                    canvas.drawRoundRect(offsetX - 10, top - 4, offsetX + wordLength * cellSize + 10, bottom + 4, 12f, 12f, paint)
                    paint.clearShadowLayer()
                }
            }
        }
        
        if (gameOver) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#CC000000")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.textAlign = Paint.Align.CENTER
            val msg = if (win) "VICTORY!" else "WORD WAS: $targetWord"
            canvas.drawText(msg, width / 2f, height / 2f, paint)
        }
    }

    private fun getStatusColor(char: Char, pos: Int, attemptRow: Int): Int {
        if (char == ' ' || attemptRow >= currentAttempt && !gameOver) return Color.DKGRAY
        if (targetWord[pos] == char) return Color.parseColor("#6AAA64")
        if (targetWord.contains(char)) return Color.parseColor("#C9B458")
        return Color.parseColor("#3A3A3C")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (gameOver) return super.onKeyDown(keyCode, event)
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> return false // Escape to header
            in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z -> addChar((keyCode - KeyEvent.KEYCODE_A + 'A'.toInt()).toChar())
            KeyEvent.KEYCODE_DEL -> deleteChar()
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> submitGuess()
            else -> return super.onKeyDown(keyCode, event)
        }
        invalidate()
        return true
    }

    private fun addChar(c: Char) {
        if (guesses[currentAttempt].length < wordLength) {
            guesses[currentAttempt] += c
            animateLetterPop(guesses[currentAttempt].length - 1, currentAttempt)
        }
    }

    private fun animateLetterPop(x: Int, y: Int) {
        lastPopX = x
        lastPopY = y
        val animator = android.animation.ValueAnimator.ofFloat(1.0f, 1.3f, 1.0f)
        animator.duration = 150
        animator.addUpdateListener { 
            letterPopScale = it.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    private fun deleteChar() {
        if (guesses[currentAttempt].isNotEmpty()) guesses[currentAttempt] = guesses[currentAttempt].dropLast(1)
    }

    private fun submitGuess() {
        if (guesses[currentAttempt].length == wordLength) {
            if (guesses[currentAttempt] == targetWord) {
                win = true; gameOver = true; onWin?.invoke(currentAttempt + 1)
            } else if (++currentAttempt == maxAttempts) {
                gameOver = true; onLose?.invoke()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) performClick()
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
