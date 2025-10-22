package net.youapps.transport.data.transport

import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Position
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.TripOptions
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.EstimatedDateTime
import net.youapps.transport.data.transport.model.GeoCoordinate
import net.youapps.transport.data.transport.model.IndividualType
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.Product
import net.youapps.transport.data.transport.model.Stop
import net.youapps.transport.data.transport.model.TransportLine
import net.youapps.transport.data.transport.model.Trip
import net.youapps.transport.data.transport.model.TripLeg
import net.youapps.transport.toJavaDate
import net.youapps.transport.toZonedDateTime
import java.time.ZonedDateTime
import java.util.Date

class PTETransportProvider(private val network: NetworkProvider) : TransportProvider {
    override suspend fun queryStations(query: String): List<Location> {
        return network.suggestLocations(query, setOf(LocationType.ANY), 10)
            ?.suggestedLocations?.mapNotNull { it.location }.orEmpty()
            .map { it.toLocation() }
    }

    override suspend fun queryDepartures(location: Location, maxAmount: Int): List<Departure> {
        return network
            .queryDepartures(location.id, Date(), maxAmount, true)
            .stationDepartures
            .orEmpty()
            .flatMap { it.departures }
            .map { dep ->
                Departure(
                    line = dep.line.toTransportLine(),
                    departure = EstimatedDateTime(
                        planned = dep.plannedTime?.toZonedDateTime(),
                        predicted = dep.predictedTime?.toZonedDateTime()
                    ),
                    destination = dep.destination!!.toLocation(),
                    platform = dep.position?.format(),
                    message = dep.message
                )
            }
    }

    override suspend fun queryTrips(
        origin: Location,
        destination: Location,
        departureTime: ZonedDateTime?,
        arrivalTime: ZonedDateTime?,
        products: Set<Product>,
        nextPagePagination: Any?,
        prevPagePagination: Any?
    ): TripsResponse {
        val originLocationType = LocationType.valueOf(origin.type.name)
        val destinationLocationType = LocationType.valueOf(destination.type.name)

        val response = if (nextPagePagination != null) {
            network.queryMoreTrips(nextPagePagination as QueryTripsContext?, true)
        } else if (prevPagePagination != null) {
            network.queryMoreTrips(prevPagePagination as QueryTripsContext?, false)
        } else {
            network.queryTrips(
                de.schildbach.pte.dto.Location(originLocationType, origin.id), // start
                null, // via
                de.schildbach.pte.dto.Location(destinationLocationType, destination.id), // end
                departureTime?.toJavaDate() ?: arrivalTime?.toJavaDate() ?: Date(), // date
                arrivalTime == null, // is date departure date?
                TripOptions(
                    products.map {
                        de.schildbach.pte.dto.Product.valueOf(it.name)
                    }.toMutableSet(),
                    null,
                    null,
                    null,
                    null
                ) // advanced trip options
            )
        }
        val trips = response.trips.orEmpty().map { trip ->
            Trip(
                id = trip.id,
                from = trip.from.toLocation(),
                to = trip.to.toLocation(),
                duration = trip.duration,
                legs = trip.legs.map { leg ->
                    return@map when (leg) {
                        is de.schildbach.pte.dto.Trip.Public -> {
                            TripLeg.Public(
                                line = leg.line.toTransportLine(),
                                arrival = leg.arrivalStop.toStop(),
                                departure = leg.departureStop.toStop(),
                                intermediateStops = leg.intermediateStops?.map { it.toStop() }
                                    .orEmpty(),
                                path = leg.toCoordinateList(),
                                message = leg.message
                            )
                        }

                        is de.schildbach.pte.dto.Trip.Individual -> {
                            TripLeg.Individual(
                                path = leg.toCoordinateList(),
                                distance = leg.distance,
                                arrival = leg.arrival.toStop(leg.arrivalTime),
                                departure = leg.departure.toStop(leg.departureTime),
                                type = IndividualType.valueOf(leg.type.name)
                            )
                        }

                        else -> throw IllegalArgumentException("unsupported trip leg")
                    }
                }
            )
        }

        return TripsResponse(
            trips = trips,
            nextPagePagination = response.context,
            prevPagePagination = response.context
        )
    }

    private fun de.schildbach.pte.dto.Location.toLocation() = Location(
        id = id,
        name = uniqueShortName(),
        type = net.youapps.transport.data.transport.model.LocationType.valueOf(type.name),
        position = coord?.let { GeoCoordinate(it.lonAsDouble, it.latAsDouble) },
    )

    private fun de.schildbach.pte.dto.Stop.toStop() = Stop(
        location = location.toLocation(),
        plannedPlatform = (plannedDeparturePosition ?: plannedArrivalPosition)?.format(),
        predictedPlatform = (predictedDeparturePosition ?: predictedArrivalPosition)?.format(),
        arrivalTime = EstimatedDateTime(
            planned = plannedArrivalTime?.toZonedDateTime(),
            predicted = predictedArrivalTime?.toZonedDateTime()
        ),
        departureTime = EstimatedDateTime(
            planned = plannedDepartureTime?.toZonedDateTime(),
            predicted = predictedDepartureTime?.toZonedDateTime()
        ),
        cancelled = arrivalCancelled || departureCancelled
    )

    fun Line.toTransportLine() = TransportLine(
        id = id,
        label = label.orEmpty(),
        name = name.orEmpty(),
        type = product?.name?.let { Product.valueOf(it) },
        message = message
    )

    private fun de.schildbach.pte.dto.Location.geoPosition() =
        GeoCoordinate(lonAsDouble, latAsDouble)

    private fun de.schildbach.pte.dto.Point.geoPosition() = GeoCoordinate(lonAsDouble, latAsDouble)
    private fun de.schildbach.pte.dto.Trip.Leg.toCoordinateList(): List<GeoCoordinate> {
        return if (!this.path.isNullOrEmpty()) {
            path.map { it.geoPosition() }
        } else if (this is de.schildbach.pte.dto.Trip.Public) {
            (listOf(departure) + intermediateStops.orEmpty().map { it.location } + listOf(arrival))
                .filterNotNull()
                .map { it.geoPosition() }
        } else if (departure != null && arrival != null) {
            listOf(departure.geoPosition(), arrival.geoPosition())
        } else {
            emptyList()
        }
    }

    private fun de.schildbach.pte.dto.Location.toStop(time: Date): Stop = Stop(
        location = toLocation(),
        predictedPlatform = null,
        plannedPlatform = null,
        arrivalTime = EstimatedDateTime(planned = time.toZonedDateTime(), predicted = null),
        departureTime = EstimatedDateTime(planned = time.toZonedDateTime(), predicted = null),
        cancelled = false
    )

    private fun Position.format() =
        listOfNotNull(name, section?.let { "($it)" })
            .joinToString(" ")
            .ifEmpty { null }
}