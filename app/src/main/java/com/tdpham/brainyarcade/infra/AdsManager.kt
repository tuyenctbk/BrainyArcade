package com.tdpham.brainyarcade.infra

import android.app.Activity
import android.content.Context
import android.util.Log
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
    private var sessionStartTime: Long = 0L
    private var isSessionTracked = false

    fun initialize(context: Context) {
        if (sessionStartTime == 0L) {
            sessionStartTime = System.currentTimeMillis()
        }
        if (!isSessionTracked) {
            isSessionTracked = true
            incrementAppOpens(context)
        }

        if (isInitialized || isInitializing) return
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
            "ca-app-pub-3940256099942544/1033173712",
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

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        if (!shouldShowAds(activity)) {
            Log.d(TAG, "Ad display blocked by Remote Config criteria (installation age, open threshold, or startup delay).")
            onDismiss()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed.")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    onDismiss()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready.")
            loadInterstitial(activity)
            onDismiss()
        }
    }

    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917",
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

    fun showRewarded(activity: Activity, onRewardEarned: (Int) -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded dismissed.")
                    rewardedAd = null
                    loadRewarded(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded failed to show: ${adError.message}")
                    rewardedAd = null
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount}")
                onRewardEarned(rewardItem.amount)
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready.")
            loadRewarded(activity)
        }
    }

    private fun getAppOpens(context: Context): Int {
        val prefs = context.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("app_opens", 0)
    }

    private fun incrementAppOpens(context: Context) {
        val prefs = context.getSharedPreferences("ads_prefs", Context.MODE_PRIVATE)
        val current = prefs.getInt("app_opens", 0)
        prefs.edit().putInt("app_opens", current + 1).apply()
        Log.d(TAG, "Incremented app opens. Current: ${current + 1}")
    }

    private fun getDaysSinceInstall(context: Context): Int {
        return try {
            val installTime = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
            val diffMs = System.currentTimeMillis() - installTime
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays.toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun getSecondsInSession(): Int {
        if (sessionStartTime == 0L) return 0
        val diffMs = System.currentTimeMillis() - sessionStartTime
        return (diffMs / 1000).toInt()
    }

    fun shouldShowAds(context: Context): Boolean {
        val minDays = RemoteConfigHelper.getMinDays()
        val minOpens = RemoteConfigHelper.getMinOpens()
        val minSessionSecs = RemoteConfigHelper.getMinSessionSeconds()

        val days = getDaysSinceInstall(context)
        val opens = getAppOpens(context)
        val sessionSecs = getSecondsInSession()

        val isInstallTimePassed = days >= minDays
        val isOpenCountPassed = opens >= minOpens
        val isSessionDelayPassed = sessionSecs >= minSessionSecs

        Log.d(TAG, "Checking ads visibility conditions: " +
                "Days since install: $days/$minDays (Passed: $isInstallTimePassed), " +
                "App opens: $opens/$minOpens (Passed: $isOpenCountPassed), " +
                "Seconds in session: $sessionSecs/$minSessionSecs (Passed: $isSessionDelayPassed)")

        return isInstallTimePassed && isOpenCountPassed && isSessionDelayPassed
    }
}
