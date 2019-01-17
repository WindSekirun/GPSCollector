package com.github.windsekirun.gpscollector.di

import android.app.Application
import com.github.windsekirun.gpscollector.MainApplication

import dagger.Binds
import dagger.Module

@Module
abstract class AppBindsModule {

    @Binds
    abstract fun bindApplication(application: MainApplication): Application
}