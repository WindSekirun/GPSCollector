package com.github.windsekirun.gpscollector.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.lifecycle.LifecycleOwner
import com.github.windsekirun.baseapp.base.BaseViewModel
import com.github.windsekirun.daggerautoinject.InjectViewModel
import com.github.windsekirun.gpscollector.MainApplication
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.main.event.ControllServiceEvent
import pyxis.uzuki.live.richutilskt.impl.F0
import java.io.File
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
    val fileList = ObservableArrayList<File>()

    private val startState = ObservableInt()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadData()
    }

    fun clickStart(view: View) {
        if (startState.get() == 1) {
            showConfirmDialog(getString(R.string.confirm_stop)) { _, _ -> stopRecording() }
            stopRecording()
        } else {
            showAlertDialog(getString(R.string.confirm_start)) { _, _ ->
                requestPermission(F0 { startRecording() },
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    fun loadData() {
        val file = File(Environment.getExternalStorageDirectory(), "/GPSCollector/")
        file.mkdirs()

        val list = file.listFiles().filter { it.extension == "txt" }.toList()
        fileList.clear()
        fileList.addAll(list)
    }

    fun clickFile(file: File) {
        val uri = if (Build.VERSION.SDK_INT >= 24) {
            val authority = requireActivity().packageName + ".fileprovider"
            FileProvider.getUriForFile(requireActivity(), authority, file)
        } else {
            Uri.fromFile(file)
        }

        val myMime = MimeTypeMap.getSingleton()
        val mimeType = myMime.getMimeTypeFromExtension(file.extension)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Open with...")
        startActivity(chooser)
    }

    private fun startRecording() {
        postEvent(ControllServiceEvent(false))
        startState.set(1)
    }

    private fun stopRecording() {
        postEvent(ControllServiceEvent(true))
        startState.set(0)
    }
}