package net.youapps.transport.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.youapps.transport.data.TransportNetwork

@Composable
fun TransportNetworkRow(network: TransportNetwork, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
            onClick.invoke()
        }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = network.name)
    }
}