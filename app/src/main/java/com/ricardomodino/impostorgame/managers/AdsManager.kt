package com.ricardomodino.impostorgame.managers

import android.content.Context
import android.view.Gravity
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.ricardomodino.impostorgame.BuildConfig

object AdsManager {

    private const val PREF_NAME = "admob_prefs"
    private const val KEY_ENABLED = "ads_enabled"

    fun init(context: Context) {
        MobileAds.initialize(context)
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    // Crea y carga un banner en el FrameLayout raíz de la actividad (anclado abajo)
    fun attachBanner(context: Context, root: FrameLayout, adUnitId: String): AdView? {
        if (!isEnabled(context)) return null
        val adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            this.adUnitId = adUnitId
        }
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        )
        root.addView(adView, params)
        adView.loadAd(AdRequest.Builder().build())
        return adView
    }
}
