package net.youapps.transport.data.transport.model

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class GeoCoordinate(
    val longitude: Double,
    val latitude: Double
) {
    /**
     * Calculate the Euklidean distance between two [GeoCoordinate]'s.
     */
    operator fun minus(other: GeoCoordinate): Double {
        return sqrt((this.latitude - other.latitude).pow(2) + (this.longitude - other.longitude).pow(2))
    }
}