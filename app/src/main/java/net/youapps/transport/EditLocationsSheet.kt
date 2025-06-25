package net.youapps.transport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schildbach.pte.dto.Location
import net.youapps.transport.components.generic.DismissBackground
import net.youapps.transport.extensions.displayName
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditLocationsSheet(
    locations: List<Location>,
    onLocationsUpdated: (List<Location>) -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = stringResource(R.string.edit_locations),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(10.dp))

        val lazyListState = rememberLazyListState()
        val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val updatedLocations = locations.toMutableList().apply {
                set(from.index, locations[to.index])
                set(to.index, locations[from.index])
            }

            onLocationsUpdated(updatedLocations)
        }

        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxWidth()) {
            items(locations, key = { it.id.orEmpty() }) { location ->
                ReorderableItem(
                    reorderableLazyListState,
                    key = location.id.orEmpty()
                ) { isDragging ->
                    val dismissBoxState = rememberSwipeToDismissBoxState()

                    SwipeToDismissBox(
                        state = dismissBoxState,
                        backgroundContent = { DismissBackground() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = location.displayName()
                            )

                            IconButton(
                                modifier = Modifier.draggableHandle(),
                                onClick = {}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}