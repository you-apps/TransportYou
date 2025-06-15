package net.youapps.transport.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NoTransfer
import androidx.compose.runtime.Composable
import de.schildbach.pte.dto.Trip
import net.youapps.transport.TextUtils

val individualIcons = mapOf(
    Trip.Individual.Type.TRANSFER to Icons.Default.NoTransfer,
    Trip.Individual.Type.WALK to Icons.AutoMirrored.Filled.DirectionsWalk,
    Trip.Individual.Type.BIKE to Icons.AutoMirrored.Filled.DirectionsBike,
    Trip.Individual.Type.CAR to Icons.Filled.DirectionsCar
)

@Composable
fun IndividualTripCard(leg: Trip.Individual) {
    CardWithIcon(individualIcons[leg.type], TextUtils.formatDistance(leg.distance))
}