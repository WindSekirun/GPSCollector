package com.github.windsekirun.gpscollector.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.github.windsekirun.baseapp.module.location.LocationTracker
import com.github.windsekirun.baseapp.utils.catchAll
import com.github.windsekirun.baseapp.utils.safeDispose
import com.github.windsekirun.baseapp.utils.subscribe
import com.github.windsekirun.daggerautoinject.InjectService
import com.github.windsekirun.gpscollector.R
import com.github.windsekirun.gpscollector.item.GeoItem
import com.github.windsekirun.gpscollector.main.MainActivity
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
                val title = intent.getStringExtra(REQUEST_STOP_TITLE) ?: ""
                requestStopService(title)
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

        requestStartService()
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
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            manager.createNotificationChannel(channel)
        }

        val colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val notification = NotificationCompat.Builder(this, id)
            .setContentText(getString(R.string.gps_service_running))
            .setSmallIcon(R.drawable.ic_pushicon)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(pendingIntent)
            .setColor(colorPrimary)
            .setColorized(true)
            .setWhen(System.currentTimeMillis())
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        startTracking()
    }

    private fun requestStopService(requestTitle: String) {
        stopTracking()

        val content = locations.joinToString("\n") { "${it.first.asDateString("HH:mm:ss.SSS")} | ${it.second}" }
        val fileName = if (requestTitle.isNotEmpty()) {
            "$requestTitle.txt"
        } else {
            "${System.currentTimeMillis().asDateString()}.txt"
        }

        val file = File(Environment.getExternalStorageDirectory(), "/GPSCollector/$fileName")
        file.parentFile.mkdirs()

        saveFile(file.absolutePath, content)
        EventBus.getDefault().post(ReloadListEvent())
        runDelayedOnUiThread({
            stopForeground(true)
            unregisterReceiver(broadcastReceiver)
            stopSelf()
        }, 500)
    }

    private fun startTracking() {
        locations.clear()

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
        const val REQUEST_STOP_TITLE = "92c812ae-44c8-4e5d-8d15-06a6a4a18f38"

        fun startService(activity: Activity) {
            val intent = Intent(activity, LocationTrackingService::class.java)

            if (Build.VERSION.SDK_INT >= 26) {
                activity.startForegroundService(intent)
            } else {
                activity.startService(intent)
            }

            activity.sendBroadcast(Intent(REQUEST_START))
        }

        fun stopService(activity: Activity, title: String = "") {
            val intent = Intent(REQUEST_STOP).apply {
                putExtra(REQUEST_STOP_TITLE, title)
            }
            activity.sendBroadcast(intent)
        }
    }
}