package net.youapps.transport.components.generic

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FilterChipWithIcon(
    isSelected: Boolean,
    label: String,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = {
            Text(label)
        },
        leadingIcon = {
            if (isSelected) Icon(
                imageVector = Icons.Default.Done, contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            ) else null
        }
    )
}