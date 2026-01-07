package ru.itis.core.analytics

import androidx.lifecycle.ViewModel

abstract class BaseViewModel(
    protected val analyticsHelper: AnalyticsHelper,
    protected val crashlyticsHelper: CrashlyticsHelper,
    protected val performanceHelper: PerformanceHelper
) : ViewModel() {

    protected fun logScreenView(screenName: String, screenClass: String) {
        analyticsHelper.logScreenView(screenName, screenClass)
    }

    protected fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        analyticsHelper.logEvent(eventName, params)
    }

    protected fun logError(throwable: Throwable) {
        crashlyticsHelper.logException(throwable)
    }

    protected fun logCrashlyticsMessage(message: String) {
        crashlyticsHelper.log(message)
    }

    override fun onCleared() {
        super.onCleared()
        logEvent("screen_exit", mapOf(
            "screen_class" to (this::class.simpleName ?: "Unknown")
        ))
    }
}