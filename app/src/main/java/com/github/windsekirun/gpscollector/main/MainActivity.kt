package com.github.windsekirun.gpscollector.main

import android.os.Bundle

import com.github.windsekirun.baseapp.base.BaseActivity
import com.github.windsekirun.daggerautoinject.InjectActivity
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.databinding.MainActivityBinding
import com.github.windsekirun.gpscollector.main.event.ClickEntryItemEvent
import com.github.windsekirun.gpscollector.main.event.ControllServiceEvent
import com.github.windsekirun.gpscollector.main.event.ReloadListEvent
import com.github.windsekirun.gpscollector.service.LocationTrackingService
import org.greenrobot.eventbus.Subscribe

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

        LocationTrackingService.startService(this)
        initRecyclerView(mBinding.recyclerView, FileListItemAdapter::class.java)
    }

    @Subscribe
    fun onControllServiceEvent(event: ControllServiceEvent) {
        if (event.stop) {
            LocationTrackingService.stopTracking(this)
        } else {
            LocationTrackingService.startTracking(this)
        }
    }

    @Subscribe
    fun onReloadListEvent(event: ReloadListEvent) {
        viewModel.loadData()
    }

    @Subscribe
    fun onClickEntryItemEvent(event: ClickEntryItemEvent) {
        viewModel.clickFile(event.file)
    }
}