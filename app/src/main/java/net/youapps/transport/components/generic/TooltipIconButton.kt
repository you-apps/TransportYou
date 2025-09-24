package net.youapps.transport.components.generic

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    filled: Boolean = false,
    onClick: () -> Unit
) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = tooltipState
    ) {
        if (filled) {
            FilledIconButton(
                onClick = onClick
            ) {
                Icon(imageVector, contentDescription)
            }
        } else {
            IconButton(
                onClick = onClick
            ) {
                Icon(imageVector, contentDescription)
            }
        }
    }
}