package net.youapps.transport.components.directions

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
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
import net.youapps.transport.components.generic.AutoRefreshingText
import net.youapps.transport.components.generic.RefreshLoadingBox
import net.youapps.transport.components.generic.RefreshLoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsBottomSheet(
    trip: Trip,
    onLocationClick: (Location) -> Unit,
    refreshLoadingState: RefreshLoadingState,
    onRefresh: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, dragHandle = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // approx size of refresh loading box at other side, to make things symmetric
            Spacer(modifier = Modifier.width(40.dp))

            BottomSheetDefaults.DragHandle()

            RefreshLoadingBox(refreshLoadingState, onRefresh)
        }
    }) {
        LazyColumn {
            item {
                TripSummary(trip)

                Spacer(modifier = Modifier.height(10.dp))
            }

            items(trip.legs.filterNot { it.shouldSkip() }) { leg ->
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
        AutoRefreshingText {
            DateUtils.getRelativeTimeSpanString(trip.firstDepartureTime.time).toString()
        }

        Card {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                text = "${stringResource(R.string.total)}: ${TextUtils.prettifyDuration(trip.duration)}"
            )
        }
    }
}