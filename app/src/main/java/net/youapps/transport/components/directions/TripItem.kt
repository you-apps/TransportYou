package net.youapps.transport.components.directions

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.youapps.transport.R
import net.youapps.transport.TextUtils
import net.youapps.transport.components.generic.AutoRefreshingText
import net.youapps.transport.data.transport.model.Trip
import net.youapps.transport.data.transport.model.TripLeg

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripItem(
    trip: Trip,
    onTripClick: () -> Unit,
) {
    val isTripCancelled = trip.legs.filterIsInstance<TripLeg.Public>()
        .any { leg -> leg.departure.cancelled }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTripClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AutoRefreshingText(
            style = MaterialTheme.typography.labelSmallEmphasized
        ) {
            trip.firstDepartureTime?.toInstant()?.toEpochMilli()
                ?.let { DateUtils.getRelativeTimeSpanString(it) }
                .toString()
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val departure = trip.legs.firstOrNull()?.departure
            Text(
                text = TextUtils.displayDepartureTimeWithDelay(
                    departure?.departureTime?.planned,
                    departure?.departureTime?.predicted
                )
            )

            Text(text = trip.from.name)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    text = "(${TextUtils.prettifyDurationShortText(trip.duration)})",
                    style = MaterialTheme.typography.labelSmallEmphasized
                )
            }

            items(trip.legs.filterNot { it.isPlatformSwitchOnly() }) { leg ->
                when (leg) {
                    is TripLeg.Public -> {
                        leg.line?.let { TransportLineCard(it) }
                    }

                    is TripLeg.Individual -> {
                        IndividualTripCard(leg)
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val arrival = trip.lastPublicLeg?.arrival

            Text(
                text = TextUtils.displayDepartureTimeWithDelay(
                    arrival?.arrivalTime?.planned,
                    arrival?.arrivalTime?.predicted
                )
            )

            Text(text = trip.to.name)
        }

        if (isTripCancelled) {
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
                    text = stringResource(R.string.trip_cancelled),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TripSummary(trip: Trip) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AutoRefreshingText {
            trip.firstDepartureTime?.toInstant()?.toEpochMilli()
                ?.let { DateUtils.getRelativeTimeSpanString(it) }.toString()
        }

        Card {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                text = "${stringResource(R.string.total)}: ${TextUtils.prettifyDurationShortText(trip.duration)}"
            )
        }
    }
}