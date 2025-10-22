package net.youapps.transport

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import net.youapps.transport.data.transport.model.GeoCoordinate
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.LocationType

object NavRoutes {
    @Serializable
    object Home

    @Serializable
    object Directions

    @Serializable
    @Parcelize
    data class DeparturesFromLocation(
        val id: String?,
        val name: String,
        val type: LocationType,
        val coordLat: Double? = null,
        val coordLon: Double? = null,
    ): Parcelable {
        constructor(location: Location) : this(
            id = location.id,
            name = location.name,
            type = location.type,
            coordLat = location.position?.latitude,
            coordLon = location.position?.longitude,
        )

        fun toLocation() = Location(
            id = id,
            name = name,
            type = type,
            position = if (coordLon != null && coordLat != null) GeoCoordinate(coordLon, coordLat) else null
        )
    }

    @Serializable
    data class TripDetails(val tripId: String)

    @Serializable
    object Settings
}
