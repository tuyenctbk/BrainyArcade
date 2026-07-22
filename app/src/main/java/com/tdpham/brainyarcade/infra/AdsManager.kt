package com.tdpham.brainyarcade.infra

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdsManager {
    private const val TAG = "AdsManager"
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInitializing = false
    private var isInitialized = false
    var sessionStartTime: Long = 0L
    private var isSessionTracked = false
    
    // Ad frequency control
    private var lastAdShowTime: Long = 0L  // Initialize to 0 to allow first ad immediately
    private var adsShownInSession = 0

    fun initialize(context: Context) {
        if (isInitialized || isInitializing) return
        
        val currentTime = System.currentTimeMillis()
        sessionStartTime = currentTime
        adsShownInSession = 0
        Log.d(TAG, "New session started. Ad counter reset.")

        if (!isSessionTracked) {
            isSessionTracked = true
            incrementAppOpens(context)
        }

        isInitializing = true
        MobileAds.initialize(context) { status ->
            isInitialized = true
            isInitializing = false
            Log.d(TAG, "AdMob Initialized: $status")
            loadInterstitial(context)
            loadRewarded(context)
        }
    }

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            "ca-app-pub-5190563950149825/5666109817", // Interstitial Ads Unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded successfully.")
                    interstitialAd = ad
                }
            }
        )
    }

    @Synchronized
    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        val maxAds = RemoteConfigHelper.getMaxPerSession()
        // Check ad frequency caps before showing
        if (adsShownInSession >= maxAds) {
            Log.d(TAG, "Ad skipped: Max ads per session ($maxAds) reached. Shown: $adsShownInSession")
            onDismiss()
            return
        }

        val cooldownMs = RemoteConfigHelper.getCooldownSeconds() * 1000L
        val timeSinceLastAd = System.currentTimeMillis() - lastAdShowTime
        if (lastAdShowTime != 0L && timeSinceLastAd < cooldownMs) {
            Log.d(TAG, "Ad skipped: Cooldown active. Last ad shown ${timeSinceLastAd / 1000}s ago. Min required: ${cooldownMs / 1000}s")
            onDismiss()
            return
        }

        if (!shouldShowAds(activity)) {
            Log.d(TAG, "Ad display blocked by Remote Config criteria (installation age, open threshold, or startup delay).")
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            // Save state in case showing the ad fails
            val previousAdShowTime = lastAdShowTime
            // Increment BEFORE showing to prevent race condition
            adsShownInSession++
            lastAdShowTime = System.currentTimeMillis()
            Log.d(TAG, "Ad will be shown (${adsShownInSession}/$maxAds)")
            
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed. Final count: $adsShownInSession")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    synchronized(AdsManager) {
                        adsShownInSession = maxOf(0, adsShownInSession - 1)
                        lastAdShowTime = previousAdShowTime
                    }
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready. Skipping and pre-loading for next time.")
            loadInterstitial(activity)
            onDismiss()
        }
    }

    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-5190563950149825/4353028148", // Reward Ads Unit ID
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Rewarded failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded loaded successfully.")
                    rewardedAd = ad
                }
            }
        )
    }

    @Synchronized
    fun showRewarded(activity: Activity, onRewardEarned: (Int) -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            // Save state in case showing the ad fails
            val previousAdShowTime = lastAdShowTime
            // Update cooldown before showing to prevent immediate interstitials after rewarded ad
            lastAdShowTime = System.currentTimeMillis()
            
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded dismissed.")
                    rewardedAd = null
                    loadRewarded(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded failed to show: ${adError.message}")
                    rewardedAd = null
                    synchronized(AdsManager) {
                        lastAdShowTime = previousAdShowTime
                    }
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount}")
                onRewardEarned(rewardItem.amount)
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready. Toasting and reloading.")
            Toast.makeText(activity, "Ad not ready, please try again in a few seconds", Toast.LENGTH_SHORT).show()
            loadRewarded(activity)
        }
    }

    private fun getAppOpens(context: Context): Int {
        val prefs = context.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("app_opens", 0)
    }

    private fun incrementAppOpens(context: Context) {
        try {
            val prefs = context.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
            val current = prefs.getInt("app_opens", 0)
            prefs.edit().putInt("app_opens", current + 1).apply()
            Log.d(TAG, "Incremented app opens. Current: ${current + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to increment app opens: ${e.message}")
        }
    }

    private fun getDaysSinceInstall(context: Context): Int {
        return try {
            val installTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            
            // Validate install time
            if (installTime <= 0) {
                Log.w(TAG, "Invalid install time: $installTime, assuming fresh install")
                return 0
            }
            
            val diffMs = System.currentTimeMillis() - installTime
            
            // Check for clock skew (if negative, assume fresh install)
            if (diffMs < 0) {
                Log.w(TAG, "Negative time difference ($diffMs ms), clock may have been adjusted backwards")
                return 0
            }
            
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get install time: ${e.message}")
            0
        }
    }

    private fun getSecondsInSession(): Int {
        if (sessionStartTime == 0L) return 0
        val diffMs = System.currentTimeMillis() - sessionStartTime
        return (diffMs / 1000).toInt()
    }

    @Synchronized
    fun shouldShowAds(context: Context): Boolean {
        val minDays = RemoteConfigHelper.getMinDays()
        val minOpens = RemoteConfigHelper.getMinOpens()
        val minSessionSecs = RemoteConfigHelper.getMinSessionSeconds()

        // Use maxOf to ensure no negative values from clock skew
        val days = maxOf(0, getDaysSinceInstall(context))
        val opens = maxOf(0, getAppOpens(context))
        val sessionSecs = maxOf(0, getSecondsInSession())

        val isInstallTimePassed = days >= minDays
        val isOpenCountPassed = opens >= minOpens
        val isSessionDelayPassed = sessionSecs >= minSessionSecs

        Log.d(TAG, "Checking ads visibility conditions: " +
                "Days since install: $days/$minDays (Passed: $isInstallTimePassed), " +
                "App opens: $opens/$minOpens (Passed: $isOpenCountPassed), " +
                "Seconds in session: $sessionSecs/$minSessionSecs (Passed: $isSessionDelayPassed)")

        return isInstallTimePassed && isOpenCountPassed && isSessionDelayPassed
    }

    fun resetSessionAdCounter() {
        adsShownInSession = 0
        Log.d(TAG, "Session ad counter reset.")
    }
}
