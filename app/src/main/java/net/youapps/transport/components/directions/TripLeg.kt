package net.youapps.transport.components.directions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Trip
import net.youapps.transport.TextUtils
import net.youapps.transport.extensions.displayName
import net.youapps.transport.toZonedDateTime

enum class StopType {
    Departure,
    Intermediate,
    Arrival
}

fun Trip.Leg.shouldSkip(): Boolean {
    return when (this) {
        is Trip.Public -> false
        is Trip.Individual -> arrival.displayName() == departure.displayName()
        else -> throw IllegalArgumentException("Unknown leg type")
    }
}

@Composable
fun TripLegPublic(leg: Trip.Public, onLocationClick: (Location) -> Unit) {
    var showIntermediateStops by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showIntermediateStops = !showIntermediateStops
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TransportLineCard(leg.line)

            Text(
                modifier = Modifier.weight(1f),
                text = leg.destination?.displayName().orEmpty()
            )

            Card {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    text = TextUtils.prettifyDuration(
                        leg.arrivalTime.time - leg.departureTime.time
                    )
                )
            }
        }

        StopRow(leg.departureStop, StopType.Departure, onLocationClick)

        AnimatedVisibility(showIntermediateStops && !leg.intermediateStops.isNullOrEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (stop in leg.intermediateStops.orEmpty()) {
                    StopRow(stop, StopType.Intermediate, onLocationClick)
                }
            }
        }

        StopRow(leg.arrivalStop, StopType.Arrival, onLocationClick)

        if (leg.message != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(16.dp),
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )

                Text(
                    text = leg.message!!,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StopRow(stop: Stop, type: StopType, onLocationClick: (Location) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        val isCancelled =
            if (type == StopType.Departure) stop.departureCancelled else stop.arrivalCancelled

        var timeStyle = if (type == StopType.Intermediate) {
            MaterialTheme.typography.bodySmall
        } else {
            MaterialTheme.typography.bodyMedium
        }
        if (isCancelled) {
            timeStyle = timeStyle.copy(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.error
            )
        }
        var locationStyle = MaterialTheme.typography.bodyLarge
        if (isCancelled) {
            locationStyle = locationStyle.copy(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.error
            )
        }

        Column {
            if (type in arrayOf(StopType.Arrival, StopType.Intermediate)) {
                Text(
                    text = TextUtils.displayDepartureTimeWithDelay(
                        stop.plannedArrivalTime,
                        stop.predictedArrivalTime
                    ),
                    style = timeStyle
                )
            }

            if (type in arrayOf(StopType.Departure, StopType.Intermediate)) {
                Text(
                    text = TextUtils.displayDepartureTimeWithDelay(
                        stop.plannedDepartureTime,
                        stop.predictedDepartureTime
                    ),
                    style = timeStyle
                )
            }
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stop.location.displayName(),
            style = locationStyle,
            overflow = TextOverflow.Ellipsis
        )

        val position =
            if (type == StopType.Departure) stop.departurePosition ?: stop.arrivalPosition
            else stop.arrivalPosition ?: stop.departurePosition

        val isPositionChanged =
            if (type == StopType.Departure) stop.predictedArrivalPosition != null && stop.predictedDeparturePosition != stop.plannedDeparturePosition
            else stop.predictedArrivalPosition != null && stop.plannedArrivalPosition != stop.predictedArrivalPosition

        if (position != null) {
            Card(
                colors = if (!isPositionChanged) CardDefaults.cardColors()
                else CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    text = position.name.orEmpty()
                )
            }
        }
    }
}

@Composable
fun TripLegIndividual(leg: Trip.Individual, onLocationClick: (Location) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IndividualTripCard(leg)

            Card {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    text = TextUtils.prettifyDuration(
                        leg.arrivalTime.time - leg.departureTime.time
                    )
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(TextUtils.formatTime(leg.departureTime.toZonedDateTime()))

            Text(leg.departure.displayName())
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(TextUtils.formatTime(leg.arrivalTime.toZonedDateTime()))

            Text(leg.arrival.displayName())
        }
    }
}