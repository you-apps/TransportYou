package net.youapps.transport.data.transport.model

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class Trip(
    val id: String,
    val from: Location,
    val to: Location,
    val legs: List<TripLeg>,
    val duration: Long,
) {
    val firstPublicLeg get() = legs.firstOrNull { it is TripLeg.Public }
    val lastPublicLeg get() = legs.lastOrNull { it is TripLeg.Public }

    val firstDepartureTime get() = firstPublicLeg?.departure?.departureTime?.predictedOrPlanned
    val lastArrivalTime get() = lastPublicLeg?.arrival?.arrivalTime?.predictedOrPlanned

    val numChanges get() = legs.filter { it is TripLeg.Public }.size - 1
}

enum class IndividualType {
    WALK,
    BIKE,
    CAR,
    TRANSFER,
    CHECK_IN,
    CHECK_OUT;
}

sealed class TripLeg {
    data class Public(
        val line: TransportLine?,
        override val departure: Stop,
        override val arrival: Stop,
        val intermediateStops: List<Stop>,
        override val path: List<GeoCoordinate>?,
        val message: String?
    ): TripLeg()

    data class Individual(
        override val departure: Stop,
        override val arrival: Stop,
        val distance: Int,
        override val path: List<GeoCoordinate>?,
        val type: IndividualType
    ): TripLeg()

    abstract val departure: Stop
    abstract val arrival: Stop
    abstract val path: List<GeoCoordinate>?

    val firstPredictedDepartureTime: ZonedDateTime?
        get() = departure.departureTime.predicted

    val durationMillis: Long get() = ChronoUnit.MILLIS.between(
        departure.departureTime.predictedOrPlanned,
        arrival.arrivalTime.predictedOrPlanned,
    )
}