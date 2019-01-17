package com.github.windsekirun.gpscollector.main

import com.github.windsekirun.baseapp.base.BaseViewModel
import com.github.windsekirun.bindadapters.observable.ObservableString
import com.github.windsekirun.daggerautoinject.InjectViewModel
import com.github.windsekirun.gpscollector.MainApplication

import javax.inject.Inject

/**
 * GPSCollector
 * Class: MainViewModel
 * Created by Pyxis on 2019-01-17.
 *
 *
 * Description:
 */

@InjectViewModel
class MainViewModel @Inject
constructor(application: MainApplication) : BaseViewModel(application) {
    val startButtonText = ObservableString()
}