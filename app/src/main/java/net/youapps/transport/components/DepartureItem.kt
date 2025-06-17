package net.youapps.transport.components

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
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.Departure
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Position
import de.schildbach.pte.dto.Product
import net.youapps.transport.TextUtils
import net.youapps.transport.extensions.displayName
import java.util.Date

@Composable
fun DepartureItem(departure: Departure, onDestinationClicked: (Location) -> Unit) {
    Column(
        modifier = Modifier
            .clickable {
                departure.destination?.let { onDestinationClicked(it) }
            }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(TextUtils.displayDepartureTimeWithDelay(departure.plannedTime, departure.predictedTime))

                Text(
                    text = DateUtils.getRelativeTimeSpanString(departure.time.time)
                        .toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                TransportLineCard(departure.line)

                Text(departure.destination?.displayName().orEmpty())
            }

            if (departure.position != null) {
                Card {
                    Text(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        text = departure.position?.name.orEmpty()
                    )
                }
            }
        }

        if (departure.message != null) {
            Text(
                text = departure.message!!,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview
@Composable
fun DeparturesListPreview() {
    val line = Line("12345", NetworkId.DB.name, Product.REGIONAL_TRAIN, "RB68")
    val start = Position("Pos. 3")
    val end = Location(LocationType.STATION, "endid", "Place", "This is the goal.")

    DepartureItem(
        Departure(Date(), Date(), line, start, end, intArrayOf(), "No message.")
    ) {}
}