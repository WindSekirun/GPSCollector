package com.github.windsekirun.gpscollector.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.github.windsekirun.baseapp.module.recycler.BaseRecyclerAdapter
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.databinding.MainItemBinding
import com.github.windsekirun.gpscollector.main.event.ClickEntryItemEvent
import java.io.File

/**
 * GPSCollector
 * Class: FileListItemAdapter
 * Created by Pyxis on 2019-01-17.
 *
 *
 * Description:
 */
class FileListItemAdapter : BaseRecyclerAdapter<File, MainItemBinding>() {

    override fun bind(binding: MainItemBinding, item: File, position: Int) {
        binding.item = item
    }

    override fun onClickedItem(binding: MainItemBinding, item: File, position: Int) {
        postEvent(ClickEntryItemEvent(item))
    }

    override fun onLongClickedItem(binding: MainItemBinding, item: File, position: Int): Boolean {
        return false
    }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return DataBindingUtil.inflate(inflater, R.layout.main_item, parent, false)
    }
}