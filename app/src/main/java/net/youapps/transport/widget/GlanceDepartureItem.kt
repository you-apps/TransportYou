package net.youapps.transport.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import net.youapps.transport.TextUtils
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.Location

@Composable
fun GlanceDepartureItem(departure: Departure, onDestinationClicked: (Location) -> Unit) {
    val defaultTextStyle = TextStyle(
        color = GlanceTheme.colors.onBackground
    )

    Column(
        modifier = GlanceModifier
            .cornerRadius(6.dp)
            .clickable {
                onDestinationClicked(departure.destination)
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                TextUtils.displayDepartureTimeWithDelay(
                    departure.departure.planned,
                    departure.departure.predicted
                ),
                style = defaultTextStyle
            )

            Spacer(
                modifier = GlanceModifier.width(4.dp)
            )

            departure.line.label?.let {
                Text(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    text = it,
                    style = defaultTextStyle.copy(
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )

                Spacer(
                    modifier = GlanceModifier.width(4.dp)
                )
            }

            Text(
                modifier = GlanceModifier.defaultWeight(),
                text = departure.destination.name,
                style = defaultTextStyle
            )

            if (departure.platform != null) {
                Spacer(
                    modifier = GlanceModifier.width(4.dp)
                )

                Text(
                    modifier = GlanceModifier
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    text = departure.platform,
                    style = defaultTextStyle.copy(
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
            }
        }

        if (departure.message != null) {
            Text(
                text = departure.message,
                style = defaultTextStyle.copy(
                    color = GlanceTheme.colors.error
                ),
            )
        }
    }
}