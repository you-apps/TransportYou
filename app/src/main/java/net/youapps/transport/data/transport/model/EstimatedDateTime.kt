package net.youapps.transport.data.transport.model

import java.time.ZonedDateTime

data class EstimatedDateTime(
    val planned: ZonedDateTime?,
    val predicted: ZonedDateTime?
) {
    val predictedOrPlanned get() = predicted ?: planned
}