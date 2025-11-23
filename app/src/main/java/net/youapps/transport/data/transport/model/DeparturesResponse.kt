package net.youapps.transport.data.transport.model

data class DeparturesResponse(
    val departures: List<Departure>,
    val lines: List<TransportLine>
)