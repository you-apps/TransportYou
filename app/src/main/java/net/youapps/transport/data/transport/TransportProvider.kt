package net.youapps.transport.data.transport

import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.Product
import net.youapps.transport.data.transport.model.Trip
import java.time.ZonedDateTime

interface TransportProvider {
    suspend fun queryStations(query: String): List<Location>
    suspend fun queryDepartures(location: Location, maxAmount: Int): List<Departure>
    suspend fun queryTrips(
        origin: Location,
        destination: Location,
        departureTime: ZonedDateTime?,
        arrivalTime: ZonedDateTime?,
        products: Set<Product>,
        nextPagePagination: Any? = null,
        prevPagePagination: Any? = null
    ): TripsResponse
}

class TripsResponse(
    val trips: List<Trip>,
    val nextPagePagination: Any?,
    val prevPagePagination: Any?
)