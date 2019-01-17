package com.github.windsekirun.gpscollector.repository

import android.content.Context
import com.github.windsekirun.baseapp.module.delegate.PreferenceTypeHolder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GPSCollector
 * Class: PreferenceRepositoryImpl
 * Created by Pyxis on 2019-01-17.
 *
 * Description:
 */
@Singleton
class PreferenceRepositoryImpl @Inject
constructor(context: Context) : PreferenceRepository {
    override var state: Boolean by PreferenceTypeHolder(context, STATE, false)

    companion object {
        private const val STATE = "c72c2903-89b2-47a4-835c-ds9b1384325"
    }
}