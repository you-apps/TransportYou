package net.youapps.transport.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.youapps.transport.models.DirectionsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripOptionsSheet(directionsModel: DirectionsModel, onDismissRequest: () -> Unit) {
    val state = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DirectionsModel.productTypes.entries.toList()) { (product, stringRes) ->
                    val isEnabled = directionsModel.enabledProducts.contains(product)

                    fun toggleProduct() {
                        if (isEnabled) directionsModel.enabledProducts.remove(product)
                        else directionsModel.enabledProducts.add(product)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                               toggleProduct()
                            }
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        transportIcons[product]?.let {
                            Icon(imageVector = it, contentDescription = null)
                        }

                        Text(text = stringResource(stringRes))

                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = {
                                toggleProduct()
                            }
                        )
                    }
                }
            }
        }
    }
}