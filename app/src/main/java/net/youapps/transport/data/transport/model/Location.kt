package net.youapps.transport.data.transport.model

import kotlinx.serialization.Serializable

enum class LocationType {
    ANY,
    STATION,
    POI,
    ADDRESS,
    COORD
}

@Serializable
data class Location(
    val id: String?,
    val name: String,
    val type: LocationType,
    val position: GeoCoordinate?,
)