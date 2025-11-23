package net.youapps.transport.components.directions

import android.util.Log
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
import net.youapps.transport.TextUtils
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.Stop
import net.youapps.transport.data.transport.model.TripLeg
import java.time.temporal.ChronoUnit

enum class StopType {
    Departure,
    Intermediate,
    Arrival
}

fun TripLeg.shouldSkip(): Boolean {
    return when (this) {
        is TripLeg.Public -> false
        is TripLeg.Individual -> arrival.location.name == departure.location.name
    }
}

@Composable
fun TripLegPublic(leg: TripLeg.Public, onLocationClick: (Location) -> Unit) {
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
            leg.line?.let { TransportLineCard(it) }

            Text(
                modifier = Modifier.weight(1f),
                text = leg.arrival.location.name
            )

            Card {
                leg.durationMillis?.let {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        text = TextUtils.prettifyDurationShortText(it)
                    )
                }
            }
        }

        StopRow(leg.departure, StopType.Departure, onLocationClick)

        AnimatedVisibility(showIntermediateStops && leg.intermediateStops.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (stop in leg.intermediateStops) {
                    StopRow(stop, StopType.Intermediate, onLocationClick)
                }
            }
        }

        StopRow(leg.arrival, StopType.Arrival, onLocationClick)

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
                    text = leg.message,
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
        var timeStyle = if (type == StopType.Intermediate) {
            MaterialTheme.typography.bodySmall
        } else {
            MaterialTheme.typography.bodyMedium
        }
        if (stop.cancelled) {
            timeStyle = timeStyle.copy(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.error
            )
        }
        var locationStyle = MaterialTheme.typography.bodyLarge
        if (stop.cancelled) {
            locationStyle = locationStyle.copy(
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.error
            )
        }

        Column {
            if (type in arrayOf(StopType.Arrival, StopType.Intermediate)) {
                Text(
                    text = TextUtils.displayDepartureTimeWithDelay(
                        stop.arrivalTime.planned,
                        stop.arrivalTime.predicted
                    ),
                    style = timeStyle
                )
            }

            if (type in arrayOf(StopType.Departure, StopType.Intermediate)) {
                Text(
                    text = TextUtils.displayDepartureTimeWithDelay(
                        stop.departureTime.planned,
                        stop.departureTime.predicted
                    ),
                    style = timeStyle
                )
            }
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stop.location.name,
            style = locationStyle,
            overflow = TextOverflow.Ellipsis
        )

        val isPositionChanged =
            stop.predictedPlatform != null && stop.predictedPlatform != stop.plannedPlatform

        (stop.predictedPlatform ?: stop.plannedPlatform)?.let { platform ->
            Card(
                colors = if (!isPositionChanged) CardDefaults.cardColors()
                else CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    text = platform
                )
            }
        }
    }
}

@Composable
fun TripLegIndividual(leg: TripLeg.Individual, onLocationClick: (Location) -> Unit) {
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
                leg.durationMillis?.let {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        text = TextUtils.prettifyDurationShortText(it)
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            leg.departure.departureTime.predictedOrPlanned?.let {
                Text(TextUtils.formatTime(it))
            }

            Text(leg.departure.location.name)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            leg.arrival.arrivalTime.predictedOrPlanned?.let {
                Text(TextUtils.formatTime(it))
            }

            Text(leg.arrival.location.name)
        }
    }
}