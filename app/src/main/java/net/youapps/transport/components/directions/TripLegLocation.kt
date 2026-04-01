package net.youapps.transport.components.directions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.youapps.transport.TextUtils
import net.youapps.transport.data.transport.model.EstimatedDateTime
import net.youapps.transport.data.transport.model.Location

@Composable
fun TripLegLocation(
    location: Location,
    time: EstimatedDateTime,
    stopType: StopType
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = when (stopType) {
                StopType.Arrival -> Icons.Default.Flag
                StopType.Departure -> Icons.Default.LocationOn
                StopType.Intermediate -> throw IllegalArgumentException("unsupported stop type")
            },
            contentDescription = null
        )

        Text(
            text = TextUtils.displayDepartureTimeWithDelay(
                time.planned,
                time.predicted
            ),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(text = location.name)
    }
}