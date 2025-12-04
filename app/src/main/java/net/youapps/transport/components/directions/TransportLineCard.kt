package net.youapps.transport.components.directions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRailway
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.outlined.DirectionsRailway
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import net.youapps.transport.components.generic.CardWithIcon
import net.youapps.transport.data.transport.model.Product
import net.youapps.transport.data.transport.model.TransportLine

val transportIcons = mapOf(
    Product.HIGH_SPEED_TRAIN to Icons.Default.DirectionsRailway,
    Product.REGIONAL_TRAIN to Icons.Outlined.DirectionsRailway,
    Product.SUBURBAN_TRAIN to Icons.Default.Train,
    Product.TRAM to Icons.Default.Tram,
    Product.SUBWAY to Icons.Default.Subway,
    Product.BUS to Icons.Default.DirectionsBus,
    Product.CABLECAR to Icons.Default.Cable,
    Product.FERRY to Icons.Default.DirectionsBoat,
    Product.ON_DEMAND to Icons.Default.DirectionsCar
)

@Composable
fun TransportLineCard(line: TransportLine, detailed: Boolean = false) {
    val lineDescription =
        if (detailed) listOfNotNull(line.label, line.destination?.name)
            .joinToString(" ")
        else line.label

    CardWithIcon(
        imageVector = transportIcons[line.type],
        text = lineDescription
    )
}