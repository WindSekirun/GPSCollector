package com.github.windsekirun.gpscollector.item

data class GeoItem(val latitude: Double, val longitude: Double) {
    override fun toString(): String {
        return "$latitude, $longitude"
    }
}