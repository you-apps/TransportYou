package net.youapps.transport.data.transport

import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Position
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.TripOptions
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.DeparturesResponse
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
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.collections.orEmpty

class PTETransportProvider(private val network: NetworkProvider) : TransportProvider {
    override suspend fun queryStations(query: String): List<Location> {
        return network.suggestLocations(query, setOf(LocationType.ANY), 10)
            ?.suggestedLocations?.mapNotNull { it.location }.orEmpty()
            .map { it.toLocation() }
    }

    override suspend fun queryDepartures(location: Location, maxAmount: Int): DeparturesResponse {
        val stationDepartures = network
            .queryDepartures(location.id, Date(), maxAmount, true)
            .stationDepartures

        val lines = stationDepartures
            .flatMap { it.lines.orEmpty() }
            .map { it.line.toTransportLine(it.destination) }
            .ifEmpty {
                stationDepartures.flatMap { it.departures }
                    .filter { it.line != null }
                    .map { it.line.toTransportLine(it.destination) }
            }
            .distinctBy { it.id }

        val departures = stationDepartures
            .orEmpty()
            .flatMap { it.departures }
            .map { dep ->
                Departure(
                    line = dep.line.toTransportLine(dep.destination),
                    departure = EstimatedDateTime(
                        planned = dep.plannedTime?.toZonedDateTime(),
                        predicted = dep.predictedTime?.toZonedDateTime()
                    ),
                    platform = dep.position?.format(),
                    message = dep.message
                )
            }

        return DeparturesResponse(departures, lines)
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
            val legs = trip.legs.map { it.toTripLeg() }.toMutableList()
            fillWithAndFixTransferLegs(legs)

            Trip(
                id = trip.id,
                from = trip.from.toLocation(),
                to = trip.to.toLocation(),
                duration = trip.duration,
                legs = legs
            )
        }

        return TripsResponse(
            trips = trips,
            nextPagePagination = response.context,
            prevPagePagination = response.context
        )
    }

    private fun de.schildbach.pte.dto.Trip.Leg.toTripLeg() = when (this) {
        is de.schildbach.pte.dto.Trip.Public -> {
            TripLeg.Public(
                line = line.toTransportLine(destination),
                arrival = arrivalStop.toStop(),
                departure = departureStop.toStop(),
                intermediateStops = intermediateStops?.map { it.toStop() }
                    .orEmpty(),
                path = toCoordinateList(),
                message = message
            )
        }

        is de.schildbach.pte.dto.Trip.Individual -> {
            TripLeg.Individual(
                path = toCoordinateList(),
                distance = distance,
                arrival = arrival.toStop(arrivalTime),
                departure = departure.toStop(departureTime),
                type = IndividualType.valueOf(type.name)
            )
        }

        else -> throw IllegalArgumentException("unsupported trip leg")
    }

    /**
     * Add individual trip legs for each platform change.
     *
     * E.g., if there's a trip leg that arrives at platform 8 at 18:30, and the next trip leg starts
     * at platform 3 at 18:52, this inserts a individual trip leg with a duration of 22min.
     */
    private fun fillWithAndFixTransferLegs(legs: MutableList<TripLeg>) {
        var i = 0
        // we have to move by index here because we modify `legs` inside the loop
        // i.e., otherwise we would modify the iterator while reading it, which would cause undefined
        // behavior
        while (i < legs.size - 1) {
            val leg = legs[i]
            val nextLeg = legs[i + 1]

            if (leg is TripLeg.Public && nextLeg is TripLeg.Public) {
                legs.add(
                    i + 1, TripLeg.Individual(
                        departure = leg.arrival,
                        arrival = nextLeg.departure,
                        type = IndividualType.TRANSFER
                    )
                )
                i++
            } else if (leg is TripLeg.Individual) {
                // calculate approximated duration of this transfer (= end - start)
                // and set start and end time to the ones of the previous/next trip leg
                // needed because otherwise durationMillis would always equal approxDurationMillis
                val approxDuration = ChronoUnit.MILLIS.between(
                    leg.departure.departureTime.predictedOrPlanned,
                    leg.arrival.arrivalTime.predictedOrPlanned,
                )
                val startTime =
                    legs.getOrNull(i - 1)?.arrival?.arrivalTime ?: leg.departure.departureTime
                val endTime = nextLeg.departure.departureTime

                legs[i] = leg.copy(
                    approxDurationMillis = approxDuration,
                    departure = leg.departure.copy(departureTime = startTime),
                    arrival = leg.arrival.copy(arrivalTime = endTime)
                )
            }

            i++
        }
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

    fun Line.toTransportLine(destination: de.schildbach.pte.dto.Location?) = TransportLine(
        id = id,
        label = label.orEmpty(),
        type = product?.name?.let { Product.valueOf(it) },
        destination = destination?.toLocation(),
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