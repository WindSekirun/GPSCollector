@file:JvmName("FileBindExtensions")

package com.github.windsekirun.gpscollector.item

import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.github.windsekirun.baseapp.module.reference.ActivityReference
import pyxis.uzuki.live.richutilskt.utils.asDateString
import java.io.File
import kotlin.math.roundToInt


fun getReadableSize(item: File): String {
    val size = item.length()
    if (size <= 0L) return "0B"

    val units = listOf("B", "kB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroup: Int = (Math.log10(size.toDouble()) / Math.log10(1024.toDouble())).toInt()
    val result = size / Math.pow(1024.toDouble(), digitGroup.toDouble()).roundToInt()
    return "%d%s".format(result, units[digitGroup])
}


fun getReadableDate(item: File): String {
    return item.lastModified().asDateString("yyyy-MM-dd")
}

fun File.toUri(): Uri? {
    val context = ActivityReference.getContext()
    return if (Build.VERSION.SDK_INT >= 24) {
        val authority = context.packageName + ".fileprovider"
        FileProvider.getUriForFile(context, authority, this)
    } else {
        Uri.fromFile(this)
    }

}