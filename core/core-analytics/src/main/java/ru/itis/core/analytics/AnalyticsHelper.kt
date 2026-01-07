package ru.itis.core.analytics

interface AnalyticsHelper {
    fun logScreenView(screenName: String, screenClass: String)
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
    fun logCustomEvent(eventName: String, vararg params: Pair<String, Any>)
}

interface CrashlyticsHelper {
    fun logException(throwable: Throwable)
    fun log(message: String)
    fun setUserId(userId: String)
    fun setCustomKey(key: String, value: Any)
}

interface PerformanceHelper {
    fun createTrace(traceName: String): Trace
    fun startTrace(traceName: String): Trace
    fun stopTrace(trace: Trace)
    fun measureOperation(traceName: String, block: () -> Unit)
}

interface Trace {
    fun start()
    fun stop()
    fun incrementMetric(metricName: String, incrementBy: Long)
}