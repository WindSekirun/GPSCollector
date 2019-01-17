package com.github.windsekirun.gpscollector.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.github.windsekirun.baseapp.module.location.LocationTracker
import com.github.windsekirun.baseapp.utils.catchAll
import com.github.windsekirun.baseapp.utils.safeDispose
import com.github.windsekirun.baseapp.utils.subscribe
import com.github.windsekirun.daggerautoinject.InjectService
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.item.GeoItem
import com.github.windsekirun.gpscollector.main.event.ReloadListEvent
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import pyxis.uzuki.live.richutilskt.utils.asDateString
import pyxis.uzuki.live.richutilskt.utils.notificationManager
import pyxis.uzuki.live.richutilskt.utils.runDelayedOnUiThread
import pyxis.uzuki.live.richutilskt.utils.saveFile
import java.io.File
import javax.inject.Inject


/**
 * GPSCollector
 * Class: LocationTrackingService
 * Created by Pyxis on 2019-01-17.
 *
 *
 * Description:
 */

@InjectService
class LocationTrackingService : Service() {
    @Inject lateinit var locationTracker: LocationTracker

    private var disposable: Disposable? = null
    private val locations = mutableListOf<Pair<Long, GeoItem>>()

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (intent.action == null) return

            if (intent.action == REQUEST_STOP) {
                requestStopService()
            } else if (intent.action == REQUEST_START) {
                requestStartService()
            }
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()

        catchAll {
            registerReceiver(broadcastReceiver, IntentFilter().apply {
                addAction(REQUEST_START)
                addAction(REQUEST_STOP)
            })
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun requestStartService() {
        val manager = notificationManager
        val id = "GPSCollector"
        if (Build.VERSION.SDK_INT >= 26) {
            val name = getString(R.string.app_name)
            val description = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, id)
                .setContentText(getString(R.string.gps_service_running))
                .setSmallIcon(R.drawable.ic_pushicon)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

        startForeground(NOTIFICATION_ID, notification)
        startTracking()
    }

    private fun requestStopService() {
        stopTracking()
        val content = locations.joinToString("\n") { "${it.first.asDateString("HH::mm:ss.SSS")} | ${it.second}" }
        val fileName = "${System.currentTimeMillis().asDateString()}.txt";
        val file = File(Environment.getExternalStorageDirectory(), "/GPSCollector/$fileName")
        file.parentFile.mkdirs()

        saveFile(file.absolutePath, content)
        EventBus.getDefault().post(ReloadListEvent())
        runDelayedOnUiThread({
            stopForeground(true)
            stopSelf()
        }, 500)
    }

    private fun startTracking() {
        disposable = locationTracker.getUpdateLocationCallback()
                .subscribe { data, _ ->
                    if (data == null) return@subscribe
                    Log.e(TAG, "startTracking: ${data.latitude}, ${data.longitude}");
                    locations.add(System.currentTimeMillis() to GeoItem(data.latitude, data.longitude))
                }

        locationTracker.setUpdateInterval(20, 10.0f)
        locationTracker.startTracking()
    }

    private fun stopTracking() {
        locationTracker.stopTracking()
        disposable.safeDispose()
    }

    companion object {
        const val REQUEST_STOP = "165f86e7-3e78-44b0-835b-6ded765415a9"
        const val REQUEST_START = "bec35418-79d6-4cd7-b971-7af7cf70fe8f"
        const val NOTIFICATION_ID = 61421
        const val TAG = "LocationTrackingService"

        fun startService(activity: Activity) {
            val intent = Intent(activity, LocationTrackingService::class.java)

            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(intent)
            } else {
                activity.startService(intent)
            }

            activity.startService(intent)
        }

        fun startTracking(activity: Activity) {
            activity.sendBroadcast(Intent(REQUEST_START))
        }

        fun stopTracking(activity: Activity) {
            activity.sendBroadcast(Intent(REQUEST_STOP))
        }
    }
}