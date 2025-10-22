package net.youapps.transport.data.transport.model

data class Departure(
    val line: TransportLine,
    val departure: EstimatedDateTime,
    val destination: Location,
    val platform: String?,
    val message: String?,
)