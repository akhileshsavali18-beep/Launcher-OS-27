package com.example.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/** Centralized monetization guard. Premium state is checked by the caller. */
object AdsManager {
  const val BANNER_TEST_ID = "ca-app-pub-3940256099942544/9214589741"
  const val INTERSTITIAL_TEST_ID = "ca-app-pub-3940256099942544/1033173712"
  private const val PREFS = "launcher_ads"
  private const val LAUNCH_COUNT = "launch_count"
  private const val LAST_INTERSTITIAL = "last_interstitial_ms"
  private const val SHOW_EVERY_LAUNCHES = 8
  private const val MIN_COOLDOWN_MS = 120_000L

  private var interstitial: InterstitialAd? = null
  private var initialized = false

  fun initialize(context: Context) {
    if (initialized) return
    initialized = true
    MobileAds.initialize(context) { preloadInterstitial(context) }
  }

  fun preloadInterstitial(context: Context) {
    if (interstitial != null) return
    InterstitialAd.load(
      context,
      INTERSTITIAL_TEST_ID,
      AdRequest.Builder().build(),
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
        override fun onAdFailedToLoad(error: LoadAdError) { interstitial = null }
      }
    )
  }

  /** Returns true when this launch reached the interstitial threshold. */
  fun registerAppLaunch(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val count = prefs.getInt(LAUNCH_COUNT, 0) + 1
    val now = System.currentTimeMillis()
    val last = prefs.getLong(LAST_INTERSTITIAL, 0L)
    val eligible = count >= SHOW_EVERY_LAUNCHES && now - last >= MIN_COOLDOWN_MS
    prefs.edit().putInt(LAUNCH_COUNT, if (eligible) 0 else count).apply()
    return eligible
  }

  fun showInterstitial(activity: Activity, onFinished: () -> Unit = {}) {
    val ad = interstitial
    if (ad == null) {
      onFinished()
      preloadInterstitial(activity)
      return
    }
    interstitial = null
    activity.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
      .edit().putLong(LAST_INTERSTITIAL, System.currentTimeMillis()).apply()
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        onFinished()
        preloadInterstitial(activity)
      }
      override fun onAdFailedToShowFullScreenContent(error: AdError) {
        onFinished()
        preloadInterstitial(activity)
      }
    }
    ad.show(activity)
  }
}
