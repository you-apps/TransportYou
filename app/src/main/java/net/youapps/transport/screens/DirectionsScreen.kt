package net.youapps.transport.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.TextUtils
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.directions.TripItem
import net.youapps.transport.components.directions.TripOptionsSheet
import net.youapps.transport.components.generic.DateTimePickerDialog
import net.youapps.transport.components.generic.FilterChipWithIcon
import net.youapps.transport.components.generic.RefreshLoadingState
import net.youapps.transport.components.generic.TooltipExtendedFAB
import net.youapps.transport.components.generic.TooltipIconButton
import net.youapps.transport.extensions.loadPrevItems
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.LocationsModel

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
        originLocationsModel.query.emit(directionsModel.origin.value?.name)
        destinationLocationsModel.query.emit(directionsModel.destination.value?.name)
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
                val selectedDate by directionsModel.date.collectAsState()
                val isDepartureDate by directionsModel.isDepartureDate.collectAsState()
                var showDateTimePicker by rememberSaveable { mutableStateOf(false) }

                Button(
                    onClick = { showDateTimePicker = true }
                ) {
                    Text(
                        text = (stringResource(if (isDepartureDate) R.string.departure else R.string.arrival))
                                + ": " + (selectedDate?.let { TextUtils.formatDateTime(it) }
                            ?: stringResource(R.string.now))
                    )
                }

                if (showDateTimePicker) {
                    DateTimePickerDialog(
                        initialValue = selectedDate,
                        onDismissRequest = { showDateTimePicker = false },
                        extraDialogContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChipWithIcon(
                                    isSelected = isDepartureDate,
                                    onSelect = {
                                        scope.launch { directionsModel.isDepartureDate.emit(true) }
                                    },
                                    label = stringResource(R.string.departure),
                                )

                                FilterChipWithIcon(
                                    isSelected = !isDepartureDate,
                                    onSelect = {
                                        scope.launch { directionsModel.isDepartureDate.emit(false) }
                                    },
                                    label = stringResource(R.string.arrival),
                                )
                            }
                        }
                    ) { newDate ->
                        scope.launch { directionsModel.date.emit(newDate) }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                val isRouteSaved by directionsModel.isRouteSaved.collectAsState(false)
                AnimatedVisibility(visible = hasValidLocations) {
                    TooltipIconButton(
                        imageVector = if (isRouteSaved) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.save),
                        filled = true
                    ) {
                        if (isRouteSaved) directionsModel.removeSavedRoute()
                        else directionsModel.addSavedRoute()
                    }
                }

                val hasAnyLocation by directionsModel.hasAnyLocation.collectAsState(false)
                AnimatedVisibility(visible = hasAnyLocation) {
                    TooltipIconButton(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = stringResource(R.string.swap),
                        filled = true
                    ) {
                        directionsModel.swapOriginAndDestination()
                        syncLocationsFromDirectionsModel()
                    }
                }

                TooltipIconButton(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = stringResource(R.string.trip_options),
                    filled = true
                ) {
                    showTripOptions = true
                }
            }

            val refreshLoadingState by directionsModel.tripsLoadingState.collectAsState()
            AnimatedVisibility(refreshLoadingState == RefreshLoadingState.LOADING) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val trips by directionsModel.trips.collectAsState()
            val routesState = rememberLazyListState()
            val isTopReached by routesState.loadPrevItems()
            LaunchedEffect(routesState.canScrollForward) {
                if (!routesState.canScrollForward) directionsModel.getMoreTrips(laterTrips = true)
            }
            LaunchedEffect(isTopReached) {
                if (isTopReached) directionsModel.getMoreTrips(laterTrips = false)
            }

            var selectedBottomSheetTripId by remember {
                mutableStateOf<String?>(null)
            }
            LazyColumn(
                state = routesState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(trips, key = { arrayOf(it.id) }) { trip ->
                    HorizontalDivider()

                    TripItem(
                        trip = trip,
                        onTripClick = {
                            selectedBottomSheetTripId = trip.id
                        },
                    )
                }
            }

            trips.find { it.id == selectedBottomSheetTripId }?.let { trip->
                navController.navigate(NavRoutes.TripDetails(trip.id))
            }
        }
    }

    if (showTripOptions) {
        TripOptionsSheet(directionsModel) {
            showTripOptions = false
        }
    }
}