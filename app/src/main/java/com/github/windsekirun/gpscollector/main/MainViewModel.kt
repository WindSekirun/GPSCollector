package com.github.windsekirun.gpscollector.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.MimeTypeMap
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.github.windsekirun.baseapp.base.BaseViewModel
import com.github.windsekirun.daggerautoinject.InjectViewModel
import com.github.windsekirun.gpscollector.MainApplication
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.item.toUri
import com.github.windsekirun.gpscollector.main.event.ControllServiceEvent
import com.github.windsekirun.gpscollector.repository.PreferenceRepository
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
    val startState = ObservableBoolean()

    @Inject lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loadData()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        startState.set(preferenceRepository.state)
    }

    fun clickStart(view: View) {
        if (startState.get()) {
            MaterialDialog(requireActivity()).show {
                title(R.string.confirm_stop)
                message(R.string.confirm_message)
                input(allowEmpty = true) { _, text ->
                    stopRecording(text.toString())
                }

                negativeButton(android.R.string.cancel)
            }
            return
        }

        showConfirmDialog(getString(R.string.confirm_start)) { _, _ ->
            requestPermission(
                F0 { startRecording() },
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun loadData() {
        requestPermission(F0 {
            val file = File(Environment.getExternalStorageDirectory(), "/GPSCollector/")
            file.mkdirs()

            val list = file.listFiles()
                .filter { it.extension == "txt" }
                .sortedByDescending { it.lastModified() }
                .toList()

            fileList.clear()
            fileList.addAll(list)
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    fun clickFile(file: File) {
        val list = getStrings(R.string.main_open, R.string.main_share, R.string.file_delete)
        showSelectorDialog(list) {
            when (it) {
                0 -> openFile(file)
                1 -> shareFile(file)
                2 -> deleteFile(file)
            }
        }
    }

    fun clickShareAll() {
        requestPermission(F0 {
            val file = File(Environment.getExternalStorageDirectory(), "/GPSCollector/")
            file.mkdirs()

            val list = file.listFiles()
                .filter { it.extension == "txt" }
                .sortedByDescending { it.lastModified() }
                .map { it.toUri() }
                .toList()

            if (list.isEmpty()) return@F0

            val result = arrayListOf<Uri?>()
            result.addAll(list)

            val myMime = MimeTypeMap.getSingleton()
            val mimeType = myMime.getMimeTypeFromExtension(file.listFiles()[0].extension)

            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, result)
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, getString(R.string.share_with))
            if (intent.resolveActivity(getApplication<MainApplication>().packageManager) != null) {
                startActivity(chooser)
            }

        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun openFile(file: File) {
        val uri = file.toUri()

        val myMime = MimeTypeMap.getSingleton()
        val mimeType = myMime.getMimeTypeFromExtension(file.extension)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, getString(R.string.open_with))
        if (intent.resolveActivity(getApplication<MainApplication>().packageManager) != null) {
            startActivity(chooser)
        }
    }

    private fun shareFile(file: File) {
        val uri = file.toUri()
        val myMime = MimeTypeMap.getSingleton()
        val mimeType = myMime.getMimeTypeFromExtension(file.extension)

        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, getString(R.string.share_with))
        if (intent.resolveActivity(getApplication<MainApplication>().packageManager) != null) {
            startActivity(chooser)
        }
    }

    private fun deleteFile(file: File) {
        file.delete()
        loadData()
    }

    private fun startRecording() {
        postEvent(ControllServiceEvent(false))
        startState.set(true)
        preferenceRepository.state = true
    }

    private fun stopRecording(text: String) {
        postEvent(ControllServiceEvent(true, text))
        startState.set(false)
        preferenceRepository.state = false
    }
}