package net.youapps.transport.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun CardWithIcon(imageVector: ImageVector?, text: String?, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .clip(CardDefaults.shape)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageVector?.let { imageVector ->
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = imageVector,
                    contentDescription = null
                )
            }

            Text(
                text = text.orEmpty()
            )
        }
    }
}