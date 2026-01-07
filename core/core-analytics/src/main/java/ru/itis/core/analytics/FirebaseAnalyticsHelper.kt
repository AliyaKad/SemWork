package ru.itis.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsHelper @Inject constructor() : AnalyticsHelper {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    override fun logScreenView(screenName: String, screenClass: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        firebaseAnalytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> param(key, value)
                    is Long -> param(key, value)
                    is Double -> param(key, value)
                    is Int -> param(key, value.toLong())
                    is Boolean -> param(key, if (value) 1L else 0L)
                    else -> {
                        param(key, value.toString())
                    }
                }
            }
        }
    }

    override fun logCustomEvent(eventName: String, vararg params: Pair<String, Any>) {
        logEvent(eventName, params.toMap())
    }
}