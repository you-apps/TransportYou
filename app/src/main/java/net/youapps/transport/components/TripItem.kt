package net.youapps.transport.components

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Trip
import net.youapps.transport.TextUtils
import net.youapps.transport.extensions.displayName

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TripItem(trip: Trip, onLocationClick: (Location) -> Unit) {
    var showTripBottomSheet by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTripBottomSheet = true }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AutoRefreshingText(
            style = MaterialTheme.typography.labelSmallEmphasized
        ) {
            DateUtils.getRelativeTimeSpanString(trip.firstDepartureTime.time).toString()
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val departure = trip.firstPublicLeg?.departureStop
            Text(
                text = TextUtils.displayDepartureTimeWithDelay(
                    departure?.plannedDepartureTime,
                    departure?.predictedDepartureTime
                )
            )

            Text(text = trip.from.displayName())
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    text = "(${TextUtils.prettifyDuration(trip.duration)})",
                    style = MaterialTheme.typography.labelSmallEmphasized
                )
            }

            items(trip.legs.filterNot { it.shouldSkip() }) { leg ->
                when (leg) {
                    is Trip.Public -> {
                        TransportLineCard(leg.line)
                    }

                    is Trip.Individual -> {
                        IndividualTripCard(leg)
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var arrival = trip.lastPublicLeg?.arrivalStop

            Text(
                text = TextUtils.displayDepartureTimeWithDelay(
                    arrival?.plannedArrivalTime,
                    arrival?.predictedArrivalTime
                )
            )

            Text(text = trip.to.displayName())
        }
    }

    if (showTripBottomSheet) {
        TripDetailsBottomSheet(trip, onLocationClick) {
            showTripBottomSheet = false
        }
    }
}