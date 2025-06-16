package net.youapps.transport.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.TextUtils
import net.youapps.transport.components.DateTimePickerDialog
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.TooltipExtendedFAB
import net.youapps.transport.components.TooltipIconButton
import net.youapps.transport.components.TripItem
import net.youapps.transport.components.TripOptionsSheet
import net.youapps.transport.extensions.displayName
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.LocationsModel
import java.util.Date

@Composable
fun DirectionsScreen(
    navController: NavController,
    directionsModel: DirectionsModel,
) {
    val scope = rememberCoroutineScope()

    val originLocationsModel =
        viewModel<LocationsModel>(key = "origin", factory = LocationsModel.Factory)
    val destinationLocationsModel =
        viewModel<LocationsModel>(key = "destination", factory = LocationsModel.Factory)

    fun syncLocationsFromDirectionsModel() = scope.launch {
        originLocationsModel.query.emit(directionsModel.origin.value?.displayName())
        destinationLocationsModel.query.emit(directionsModel.destination.value?.displayName())
    }
    LaunchedEffect(Unit) {
        // set the initial input values if navigated here with already set locations
        syncLocationsFromDirectionsModel()
    }

    val hasValidLocations by directionsModel.hasValidLocations.collectAsState(false)
    var showTripOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (hasValidLocations) {
                TooltipExtendedFAB(
                    imageVector = Icons.Default.Directions,
                    contentDescription = stringResource(R.string.directions)
                ) {
                    directionsModel.queryTrips()
                }
            }
        }
    ) { pV ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pV)
        ) {
            LocationSearchBar(
                locationsModel = originLocationsModel,
                placeholder = stringResource(R.string.origin),
                leadingIcon = Icons.Default.LocationOn,
            ) {
                scope.launch { directionsModel.origin.emit(it) }
            }

            LocationSearchBar(
                locationsModel = destinationLocationsModel,
                placeholder = stringResource(R.string.destination),
                leadingIcon = Icons.Default.Flag
            ) {
                scope.launch { directionsModel.destination.emit(it) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .clip(CardDefaults.shape)
                        .clickable {
                            showDateTimePicker = true
                        }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        text = selectedDate?.let { TextUtils.formatDateTime(it) }
                            ?: stringResource(R.string.now)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TooltipIconButton(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = stringResource(R.string.swap)
                ) {
                    directionsModel.swapOriginAndDestination()
                    syncLocationsFromDirectionsModel()
                }

                TooltipIconButton(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = stringResource(R.string.trip_options)
                ) {
                    showTripOptions = true
                }
            }

            val trips by directionsModel.trips.collectAsState()
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(trips) { trip ->
                    HorizontalDivider()

                    TripItem(trip) { location ->
                        navController.navigate(NavRoutes.DeparturesFromLocation(location))
                    }
                }
            }
        }
    }

    if (showTripOptions) {
        TripOptionsSheet(directionsModel) {
            showTripOptions = false
        }
    }
}