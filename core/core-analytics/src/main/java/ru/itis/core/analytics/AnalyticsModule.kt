package ru.itis.core.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsModule {

    @Binds
    @Singleton
    fun bindAnalyticsHelper(impl: FirebaseAnalyticsHelper): AnalyticsHelper

    @Binds
    @Singleton
    fun bindCrashlyticsHelper(impl: FirebaseCrashlyticsHelper): CrashlyticsHelper

    @Binds
    @Singleton
    fun bindPerformanceHelper(impl: FirebasePerformanceHelper): PerformanceHelper
}