package com.tdpham.brainyarcade.hub

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.lifecycleScope
import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.db.AppDatabase
import com.tdpham.brainyarcade.db.RecentGame
import com.tdpham.brainyarcade.infra.OnboardingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.tdpham.brainyarcade.infra.AdsManager
import com.tdpham.brainyarcade.infra.FirebaseManager
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var rowsGrid: VerticalGridView
    private lateinit var onboardingManager: OnboardingManager
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var allGames = listOf<GameInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        onboardingManager = OnboardingManager(this)
        rowsGrid = findViewById(R.id.hub_rows)
        
        FirebaseManager.initialize(this)
        AdsManager.initialize(this)
        
        try {
            val adView = findViewById<AdView>(R.id.ad_view_hub)
            val adRequest = AdRequest.Builder().build()
            adView?.loadAd(adRequest)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading banner ad: ${e.message}")
        }
        
        initGamesList()
        setupNavigation()
        checkOnboarding()
        checkForUpdates()
    }

    private fun setupNavigation() {
        val titleView = findViewById<TextView>(R.id.main_title)
        val titleText = getString(R.string.app_name).uppercase(Locale.US)
        val spannable = SpannableStringBuilder(titleText)
        if (titleText.startsWith("BRAINY")) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.neon_cyan)),
                0, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.text_white)),
                6, titleText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        titleView.text = spannable
        titleView.setOnClickListener {
            showRecordsDialog()
        }

        findViewById<TextView>(R.id.nav_categories).setOnClickListener {
            showCategoriesSelection()
        }
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            rowsGrid.smoothScrollToPosition(0)
        }
    }

    private fun showCategoriesSelection() {
        val categories = GameCategory.entries.map { category ->
            when(category) {
                GameCategory.LOGIC -> getString(R.string.cat_logic)
                GameCategory.MATH -> getString(R.string.cat_math)
                GameCategory.SPATIAL -> getString(R.string.cat_spatial)
                GameCategory.MEMORY -> getString(R.string.cat_memory)
                GameCategory.WORDS -> getString(R.string.cat_words)
                GameCategory.STRATEGY -> getString(R.string.cat_strategy)
            }
        }.toTypedArray()
        
        AlertDialog.Builder(this, R.style.Theme_BrainyArcade_Dialog)
            .setTitle(getString(R.string.categories))
            .setItems(categories) { _, which ->
                val selectedTitle = categories[which]
                val adapter = rowsGrid.adapter as? HubRowAdapter
                if (adapter != null) {
                    val index = adapter.rows.indexOfFirst { it.title == selectedTitle }
                    if (index != -1) {
                        rowsGrid.smoothScrollToPosition(index)
                    }
                }
            }
            .show()
    }

    private fun showRecordsDialog() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MainActivity)
            val spannable = SpannableStringBuilder()
            allGames.forEach { game ->
                val scores = withContext(Dispatchers.IO) {
                    db.gameDao().getTopScores(game.id)
                }
                val progress = withContext(Dispatchers.IO) {
                    db.gameDao().getProgress(game.id)
                }
                if (scores.isNotEmpty() || progress != null) {
                    val start = spannable.length
                    val titleText = getString(game.titleResId).uppercase(Locale.US) + "\n"
                    spannable.append(titleText)
                    spannable.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.neon_cyan)),
                        start, start + titleText.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                        start, start + titleText.length - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    
                    if (progress != null) {
                        val lvlText = "  ${getString(R.string.label_level).replace("%d", progress.maxLevelReached.toString())}\n"
                        spannable.append(lvlText)
                    }
                    if (scores.isNotEmpty()) {
                        val best = if (game.scoreType == ScoreType.TIME) scores.filter { it.score > 0 }.minByOrNull { it.score }?.score ?: 0 else scores.first().score
                        if (best > 0) {
                            val label = if (game.scoreType == ScoreType.TIME) {
                                val mins = best / 60; val secs = best % 60
                                String.format(Locale.US, "%02d:%02d", mins, secs)
                            } else {
                                String.format(Locale.US, "%d", best)
                            }
                            val prefix = if (game.scoreType == ScoreType.TIME) "  BEST TIME: " else "  BEST SCORE: "
                            val scoreText = "$prefix$label\n"
                            val scoreStart = spannable.length
                            spannable.append(scoreText)
                            spannable.setSpan(
                                ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.gold)),
                                scoreStart + prefix.length, scoreStart + scoreText.length - 1,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    spannable.append("\n")
                }
            }
            val message = if (spannable.isEmpty()) SpannableStringBuilder("No records yet. Play a game to set one!") else spannable
            AlertDialog.Builder(this@MainActivity, R.style.Theme_BrainyArcade_Dialog)
                .setTitle(getString(R.string.my_records))
                .setMessage(message)
                .setPositiveButton(getString(R.string.got_it), null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDiscoveryHub()
        checkRatingSuggestion()
    }

    private fun initGamesList() {
        allGames = GameRegistry.ALL_GAMES
    }

    private fun refreshDiscoveryHub() {
        lifecycleScope.launch {
            val recents = withContext(Dispatchers.IO) { db.gameDao().getRecentGames() }
            val discoveryRows = mutableListOf<HubRow>()
            val dailyGameId = com.tdpham.brainyarcade.infra.DailyChallengeManager.getDailyGameId()

            val heroGames = mutableListOf<GameInfo>()
            allGames.find { it.id == dailyGameId }?.let { heroGames.add(it) }
            if (recents.isNotEmpty()) {
                allGames.find { it.id == recents.first().gameId }?.let { if (it.id != dailyGameId) heroGames.add(it) }
            }
            val trendingIds = listOf("sudoku", "game_2048", "minesweeper")
            trendingIds.forEach { id -> allGames.find { it.id == id }?.let { if (!heroGames.contains(it)) heroGames.add(it) } }
            discoveryRows.add(HubRow(getString(R.string.hero_selection), heroGames.take(5)))

            if (recents.isNotEmpty()) {
                val recentGames = recents.mapNotNull { recent -> allGames.find { it.id == recent.gameId } }
                discoveryRows.add(HubRow(getString(R.string.recently_played), recentGames))
            }

            GameCategory.entries.forEach { category ->
                val categoryGames = allGames.filter { it.category == category }
                if (categoryGames.isNotEmpty()) {
                    val title = when(category) {
                        GameCategory.LOGIC -> getString(R.string.cat_logic)
                        GameCategory.MATH -> getString(R.string.cat_math)
                        GameCategory.SPATIAL -> getString(R.string.cat_spatial)
                        GameCategory.MEMORY -> getString(R.string.cat_memory)
                        GameCategory.WORDS -> getString(R.string.cat_words)
                        GameCategory.STRATEGY -> getString(R.string.cat_strategy)
                    }
                    discoveryRows.add(HubRow(title, categoryGames))
                }
            }

            rowsGrid.adapter = HubRowAdapter(discoveryRows, dailyGameId) { game ->
                val seed = if (discoveryRows.firstOrNull()?.games?.firstOrNull()?.id == game.id) com.tdpham.brainyarcade.infra.DailyChallengeManager.getDailySeed() else -1L
                launchGame(game, seed)
            }
            rowsGrid.post {
                rowsGrid.requestFocus()
            }
        }
    }

    private fun checkOnboarding() {
        if (onboardingManager.isFirstRun()) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.Theme_BrainyArcade_Dialog)
                .setTitle(getString(R.string.app_name).uppercase())
                .setMessage("Welcome to Brainy Arcade! Discover 25 world-class logic puzzles. Train your mind every day and break your personal best records.")
                .setPositiveButton(getString(R.string.got_it)) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun checkRatingSuggestion() {
        if (onboardingManager.shouldShowRating()) {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this, R.style.Theme_BrainyArcade_Dialog)
                .setTitle(getString(R.string.rate_title))
                .setMessage(getString(R.string.rate_message))
                .setPositiveButton(getString(R.string.rate_now)) { _, _ ->
                    onboardingManager.markRated()
                }
                .setNegativeButton(getString(R.string.rate_later), null)
                .show()
        }
    }

    private fun checkForUpdates() {
        if (updatePromptedThisSession) return
        updatePromptedThisSession = true
        lifecycleScope.launch {
            kotlinx.coroutines.delay(2000)
            val currentVersion = packageManager.getPackageInfo(packageName, 0).versionCode
            val latestVersion = 101
            if (latestVersion > currentVersion) {
                com.google.android.material.dialog.MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_BrainyArcade_Dialog)
                    .setTitle(getString(R.string.update_available))
                    .setMessage(getString(R.string.update_msg))
                    .setPositiveButton(getString(R.string.update_now)) { _, _ -> }
                    .setNegativeButton(getString(R.string.later), null)
                    .show()
            }
        }
    }

    private fun launchGame(game: GameInfo, seed: Long = -1) {
        lifecycleScope.launch(Dispatchers.IO) { db.gameDao().updateRecentGame(RecentGame(game.id, System.currentTimeMillis())) }
        
        if (game.progressionType == ProgressionType.LEVELS && seed == -1L) {
            showLevelSelection(game)
        } else {
            startActivity(Intent(this, game.activityClass).apply { 
                if (seed != -1L) putExtra("GAME_SEED", seed)
                else putExtra("GAME_LEVEL", 1)
            })
        }
    }

    private fun showLevelSelection(game: GameInfo) {
        lifecycleScope.launch {
            val progress = withContext(Dispatchers.IO) { db.gameDao().getProgress(game.id) }
            val maxUnlocked = progress?.maxLevelReached ?: 1
            
            val levels = (1..Math.min(maxUnlocked + 1, 100)).map { 
                String.format(Locale.US, getString(R.string.label_level), it)
            }.toTypedArray()
            
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_BrainyArcade_Dialog)
                .setTitle(getString(R.string.select_level))
                .setItems(levels) { _, which ->
                    val selectedLevel = which + 1
                    startActivity(Intent(this@MainActivity, game.activityClass).apply {
                        putExtra("GAME_LEVEL", selectedLevel)
                    })
                }
                .show()
        }
    }

    companion object {
        private var updatePromptedThisSession = false
    }
}
