package com.github.windsekirun.gpscollector.di

import com.github.windsekirun.gpscollector.MainApplication
import dagger.Module
import dagger.Provides
import pyxis.uzuki.live.richutilskt.utils.RPreference
import javax.inject.Singleton

@Module
class AppProvidesModule {

    @Provides
    @Singleton
    fun provideRPerference(application: MainApplication): RPreference = RPreference.getInstance(application)

}