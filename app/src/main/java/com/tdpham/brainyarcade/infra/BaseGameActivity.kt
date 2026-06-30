package com.tdpham.brainyarcade.infra

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.db.AppDatabase
import com.tdpham.brainyarcade.db.GameScore
import com.tdpham.brainyarcade.hub.GameCategory
import com.tdpham.brainyarcade.hub.GameRegistry
import com.tdpham.brainyarcade.hub.ScoreType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Base activity for all games in BrainyArcade.
 */
abstract class BaseGameActivity : AppCompatActivity() {

    protected lateinit var soundManager: SoundManager
    protected val db by lazy { AppDatabase.getDatabase(this) }
    protected val onboardingManager by lazy { OnboardingManager(this) }
    
    abstract val gameId: String
    protected abstract fun getGameView(): GameView

    private var highScore: Int = 0
    private var scoreType: ScoreType = ScoreType.TIME
    protected var currentLevel: Int = 1
    
    private var startTime: Long = 0
    private var isTimerRunning = false
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                updateScoreDisplay(elapsed.toInt())
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager.getInstance(this)
        
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showGameMenu()
            }
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val gameInfo = GameRegistry.ALL_GAMES.find { it.id == gameId }
        scoreType = gameInfo?.scoreType ?: ScoreType.TIME
        currentLevel = intent.getIntExtra("GAME_LEVEL", 1)
        
        setupHeader()
        loadHighScore()
        
        val seed = intent.getLongExtra("GAME_SEED", -1L)
        getGameView().resetGame(seed, currentLevel)
        
        if (scoreType == ScoreType.TIME) {
            startTimer()
        }
        
        FirebaseManager.logGameStart(gameId)

        if (onboardingManager.shouldShowGameInstructions(gameId)) {
            showHelp(gameInfo?.howToPlayResId)
        }
    }

    private fun setupHeader() {
        val gameInfo = GameRegistry.ALL_GAMES.find { it.id == gameId }
        val titleView = findViewById<TextView>(R.id.header_title)
        if (titleView != null && gameInfo != null) {
            val titleText = getString(gameInfo.titleResId).uppercase(Locale.US)
            val words = titleText.split(" ")
            val spannable = SpannableStringBuilder(titleText)
            if (words.size > 1) {
                val firstWordLength = words[0].length
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.neon_cyan)),
                    0, firstWordLength,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_white)),
                    firstWordLength, titleText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.neon_cyan)),
                    0, titleText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            titleView.text = spannable
        } else {
            titleView?.text = getString(R.string.app_name).uppercase(Locale.US)
        }

        findViewById<TextView>(R.id.header_category)?.text = when(gameInfo?.category) {
            GameCategory.LOGIC -> getString(R.string.cat_logic)
            GameCategory.MATH -> getString(R.string.cat_math)
            GameCategory.SPATIAL -> getString(R.string.cat_spatial)
            GameCategory.MEMORY -> getString(R.string.cat_memory)
            GameCategory.WORDS -> getString(R.string.cat_words)
            GameCategory.STRATEGY -> getString(R.string.cat_strategy)
            else -> ""
        }
        
        if (gameInfo?.progressionType == com.tdpham.brainyarcade.hub.ProgressionType.LEVELS) {
            val levelView = findViewById<TextView>(R.id.header_level)
            levelView?.visibility = android.view.View.VISIBLE
            levelView?.text = String.format(Locale.US, getString(R.string.label_level), currentLevel)
        }
        
        findViewById<android.view.View>(R.id.header_restart)?.apply {
            setOnClickListener { restartGame() }
            isFocusable = false
            isFocusableInTouchMode = false
        }
        findViewById<android.view.View>(R.id.header_help)?.apply {
            val gameInfo = GameRegistry.ALL_GAMES.find { it.id == gameId }
            setOnClickListener { showHelp(gameInfo?.howToPlayResId) }
            isFocusable = false
            isFocusableInTouchMode = false
        }

        updateScoreDisplay(0)
    }

    private fun loadHighScore() {
        lifecycleScope.launch {
            val scores = withContext(Dispatchers.IO) {
                db.gameDao().getTopScores(gameId)
            }
            highScore = if (scoreType == ScoreType.TIME) {
                scores.filter { it.score > 0 }.minByOrNull { it.score }?.score ?: 0
            } else {
                scores.firstOrNull()?.score ?: 0
            }
            
            val bestLabel = if (scoreType == ScoreType.TIME) "BEST TIME" else "BEST"
            findViewById<TextView>(R.id.header_best)?.text = String.format(Locale.US, "%s: %s", bestLabel, formatScore(highScore))
        }
    }

    protected fun startTimer() {
        startTime = System.currentTimeMillis()
        isTimerRunning = true
        timerHandler.post(timerRunnable)
    }

    protected fun stopTimer() {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)
    }

    protected fun updateScoreDisplay(value: Int) {
        val label = when(scoreType) {
            ScoreType.POINTS -> String.format(Locale.US, "SCORE: %d", value)
            ScoreType.TIME -> String.format(Locale.US, "TIME: %s", formatScore(value))
            ScoreType.MOVES -> String.format(Locale.US, "MOVES: %d", value)
        }
        findViewById<TextView>(R.id.header_score)?.text = label
    }

    private fun formatScore(value: Int): String {
        if (value == 0) return "---"
        return if (scoreType == ScoreType.TIME) {
            val mins = value / 60
            val secs = value % 60
            String.format(Locale.US, "%02d:%02d", mins, secs)
        } else {
            String.format(Locale.US, "%d", value)
        }
    }

    protected fun onGameWin(score: Int) {
        onGameOver(score, true)
    }

    protected fun onGameOver(score: Int, won: Boolean) {
        stopTimer()
        if (won) {
            FirebaseManager.logGameWin(gameId, score)
            lifecycleScope.launch(Dispatchers.IO) {
                // 1. Save High Score
                db.gameDao().insertScore(GameScore(gameId = gameId, score = score))
                
                // 2. Update Level Progress
                val currentProgress = db.gameDao().getProgress(gameId)
                val newMax = if (currentProgress == null) currentLevel + 1 
                             else Math.max(currentProgress.maxLevelReached, currentLevel + 1)
                
                db.gameDao().updateProgress(com.tdpham.brainyarcade.db.GameProgress(
                    gameId = gameId,
                    currentLevel = currentLevel + 1,
                    maxLevelReached = newMax
                ))
            }
        }

        runOnUiThread {
            AlertDialog.Builder(this, R.style.Theme_BrainyArcade_Dialog)
                .setTitle(if (won) getString(R.string.victory) else getString(R.string.game_over))
                .setMessage(if (won) getString(R.string.victory_msg, formatScore(score)) else getString(R.string.better_luck))
                .setPositiveButton(getString(R.string.replay)) { _, _ -> restartGame() }
                .setNegativeButton(getString(R.string.quit_to_hub)) { _, _ -> exitGame() }
                .setCancelable(false)
                .show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_M -> {
                showGameMenu()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    protected open fun showGameMenu() {
        val gameInfo = GameRegistry.ALL_GAMES.find { it.id == gameId }
        val items = arrayOf(
            getString(R.string.resume),
            getString(R.string.restart),
            getString(R.string.how_to_play_title),
            getString(R.string.quit_to_hub)
        )
        AlertDialog.Builder(this, R.style.Theme_BrainyArcade_Dialog)
            .setTitle(getString(gameInfo?.titleResId ?: R.string.app_name))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> {} // Resume
                    1 -> restartGame()
                    2 -> showHelp(gameInfo?.howToPlayResId)
                    3 -> exitGame()
                }
            }
            .show()
    }

    protected fun exitGame() {
        AdsManager.showInterstitial(this) {
            finish()
        }
    }

    protected open fun restartGame() {
        getGameView().resetGame(-1L, currentLevel)
        if (scoreType == ScoreType.TIME) {
            startTimer()
        } else {
            updateScoreDisplay(0)
        }
        loadHighScore()
    }

    private fun showHelp(guideResId: Int?) {
        AlertDialog.Builder(this, R.style.Theme_BrainyArcade_Dialog)
            .setTitle(getString(R.string.how_to_play_title))
            .setMessage(if (guideResId != null) getString(guideResId) else getString(R.string.no_instr))
            .setPositiveButton(getString(R.string.got_it), null)
            .show()
    }
    
    protected fun playSfx(resId: Int) {
        soundManager.play(resId)
    }
}
