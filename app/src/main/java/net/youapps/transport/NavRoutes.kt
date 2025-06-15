package net.youapps.transport

import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Point
import de.schildbach.pte.dto.Product
import kotlinx.serialization.Serializable

object NavRoutes {
    @Serializable
    object Home

    @Serializable
    object Directions

    @Serializable
    data class DeparturesFromLocation(
        val type: LocationType? = null,
        val id: String? = null,
        val coordLat: Double? = null,
        val coordLon: Double? = null,
        val place: String? = null,
        val name: String? = null,
        val products: List<String>? = null
    ) {
        constructor(location: Location) : this(
            location.type,
            location.id,
            location.coord?.latAsDouble,
            location.coord?.lonAsDouble,
            location.place,
            location.name,
            location.products?.map { it.name }
        )

        fun toLocation() = Location(
            type, id, if (coordLon != null && coordLat != null) {
                Point.fromDouble(coordLat, coordLon)
            } else null, place, name, products?.map { Product.valueOf(it) }?.toSet()
        )
    }
}
