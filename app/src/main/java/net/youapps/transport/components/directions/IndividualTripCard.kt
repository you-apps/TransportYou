package net.youapps.transport.components.directions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NoTransfer
import androidx.compose.runtime.Composable
import net.youapps.transport.TextUtils
import net.youapps.transport.components.generic.CardWithIcon
import net.youapps.transport.data.transport.model.IndividualType
import net.youapps.transport.data.transport.model.TripLeg

val individualIcons = mapOf(
    IndividualType.TRANSFER to Icons.Default.NoTransfer,
    IndividualType.WALK to Icons.AutoMirrored.Filled.DirectionsWalk,
    IndividualType.BIKE to Icons.AutoMirrored.Filled.DirectionsBike,
    IndividualType.CAR to Icons.Filled.DirectionsCar
)

@Composable
fun IndividualTripCard(leg: TripLeg.Individual) {
    CardWithIcon(individualIcons[leg.type], TextUtils.formatDistance(leg.distance))
}