package net.youapps.transport.data.transport.model

import kotlinx.serialization.Serializable

@Serializable
data class GeoCoordinate(
    val longitude: Double,
    val latitude: Double
)