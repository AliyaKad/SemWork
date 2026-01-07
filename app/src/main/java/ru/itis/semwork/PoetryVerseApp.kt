package ru.itis.semwork

import android.app.Application
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PoetryVerseApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        if (BuildConfig.DEBUG) {
            FirebaseApp.initializeApp(this)
            com.google.firebase.perf.FirebasePerformance.getInstance()
                .isPerformanceCollectionEnabled = true
        }
    }
}