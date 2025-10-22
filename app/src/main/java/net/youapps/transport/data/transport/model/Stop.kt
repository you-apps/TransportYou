package net.youapps.transport.data.transport.model

data class Stop(
    val location: Location,
    val plannedPlatform: String?,
    val predictedPlatform: String?,
    val arrivalTime: EstimatedDateTime,
    val departureTime: EstimatedDateTime,
    val cancelled: Boolean
)