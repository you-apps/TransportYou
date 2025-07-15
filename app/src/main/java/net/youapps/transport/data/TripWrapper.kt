package net.youapps.transport.data

import de.schildbach.pte.dto.Trip

/**
 * Wrapper around [Trip] that properly implements [equals]
 *
 * This wrapper is required in order for Compose to automatically recognize trip changes.
 */
class TripWrapper(val trip: Trip) {
    val id: String? get() = trip.id

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is TripWrapper -> other.trip.id == trip.id && other.trip.legs == trip.legs
            else -> false
        }
    }

    override fun hashCode(): Int {
        return trip.id.hashCode() + 31 * trip.legs.hashCode()
    }
}