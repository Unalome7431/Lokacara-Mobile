package com.app.lokacara.data

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsTracker @Inject constructor() {

    private val tag = "LokacaraAnalytics"

    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        val paramsStr = if (params.isNotEmpty()) params.entries.joinToString(", ") { "${it.key}=${it.value}" } else ""
        Log.d(tag, "[EVENT] $name | $paramsStr")
    }

    fun logScreenView(screenName: String) {
        Log.d(tag, "[SCREEN] $screenName")
    }

    fun logClick(elementName: String, extra: Map<String, Any> = emptyMap()) {
        logEvent("click_$elementName", extra)
    }
}
