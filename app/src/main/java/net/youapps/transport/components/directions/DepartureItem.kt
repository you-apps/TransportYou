package net.youapps.transport.components.directions

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.youapps.transport.TextUtils
import net.youapps.transport.components.generic.AutoRefreshingText
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.EstimatedDateTime
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.LocationType
import net.youapps.transport.data.transport.model.Product
import net.youapps.transport.data.transport.model.TransportLine
import java.time.ZonedDateTime

@Composable
fun DepartureItem(departure: Departure, onDestinationClicked: (Location) -> Unit) {
    Column(
        modifier = Modifier
            .clickable {
                departure.line.destination?.let { onDestinationClicked(it) }
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    TextUtils.displayDepartureTimeWithDelay(
                        departure.departure.planned,
                        departure.departure.predicted
                    )
                )

                AutoRefreshingText(
                    style = MaterialTheme.typography.bodySmall
                ) {
                    departure.departure.predictedOrPlanned?.toInstant()?.toEpochMilli()
                        ?.let { DateUtils.getRelativeTimeSpanString(it) }
                        .toString()
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                TransportLineCard(departure.line)

                departure.line.destination?.name?.let { Text(it) }
            }

            if (departure.platform != null) {
                Card {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        text = departure.platform
                    )
                }
            }
        }

        if (departure.message != null) {
            Text(
                text = departure.message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

val DEMO_LOCATION = Location( "endid", "Berlin", LocationType.STATION,null)
val DEMO_DEPARTURE = Departure(
    TransportLine("12345",  "RB68", DEMO_LOCATION, Product.REGIONAL_TRAIN, null),
    departure = EstimatedDateTime(
        ZonedDateTime.now(),
        ZonedDateTime.now().plusMinutes(5)
    ),
    "Pos. 3",
    "No message."
)

@Preview
@Composable
fun DeparturesListPreview() {

    DepartureItem(DEMO_DEPARTURE) {}
}