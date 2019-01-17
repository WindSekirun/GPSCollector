package com.github.windsekirun.gpscollector.di

import com.github.windsekirun.gpscollector.MainApplication
import com.github.windsekirun.gpscollector.repository.PreferenceRepository
import com.github.windsekirun.gpscollector.repository.PreferenceRepositoryImpl
import dagger.Module
import dagger.Provides
import pyxis.uzuki.live.richutilskt.utils.RPreference
import javax.inject.Singleton

@Module
class AppProvidesModule {

    @Provides
    @Singleton
    fun provideRPerference(application: MainApplication): RPreference = RPreference.getInstance(application)

    @Provides
    @Singleton
    fun providePreferenceRepository(impl: PreferenceRepositoryImpl): PreferenceRepository {
        return impl
    }

    @Provides
    @Singleton
    fun providePreferenceRepositoryImpl(context: MainApplication): PreferenceRepositoryImpl {
        return PreferenceRepositoryImpl(context)
    }

}