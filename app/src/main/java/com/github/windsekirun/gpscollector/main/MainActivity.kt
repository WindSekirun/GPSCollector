package com.github.windsekirun.gpscollector.main

import android.os.Bundle

import com.github.windsekirun.baseapp.base.BaseActivity
import com.github.windsekirun.daggerautoinject.InjectActivity
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.databinding.MainActivityBinding

/**
 * GPSCollector
 * Class: ${NAME}
 * Created by Pyxis on 2019-01-17.
 *
 *
 * Description:
 */

@InjectActivity
class MainActivity : BaseActivity<MainActivityBinding>() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        viewModel = getViewModel(MainViewModel::class.java)
        mBinding.viewModel = viewModel
    }
}