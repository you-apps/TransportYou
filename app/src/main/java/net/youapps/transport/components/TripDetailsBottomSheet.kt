package net.youapps.transport.components

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Trip
import net.youapps.transport.R
import net.youapps.transport.TextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsBottomSheet(
    trip: Trip,
    onLocationClick: (Location) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn {
            item {
                TripSummary(trip)

                Spacer(modifier = Modifier.height(10.dp))
            }

            items(trip.legs) { leg ->
                HorizontalDivider()

                when (leg) {
                    is Trip.Public -> TripLegPublic(leg, onLocationClick)
                    is Trip.Individual -> TripLegIndividual(leg, onLocationClick)
                }
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
        Text(
            text = DateUtils.getRelativeTimeSpanString(trip.firstDepartureTime.time).toString()
        )

        Card {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                text = "${stringResource(R.string.total)}: ${TextUtils.prettifyDuration(trip.duration)}"
            )
        }
    }
}