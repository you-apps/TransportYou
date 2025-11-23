package net.youapps.transport

import io.github.dellisd.spatialk.geojson.Position
import net.youapps.transport.data.transport.model.Location

object MapLibreUtils {
    fun getMapLibreStyleUrl(isDarkTheme: Boolean): String {
        val mapStyle = if (isDarkTheme) "dark" else "liberty"
        return "https://tiles.openfreemap.org/styles/$mapStyle"
    }


    fun Location.geoPosition() = position?.let { Position(it.longitude, it.latitude) }
}