package net.youapps.transport.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.TooltipExtendedFAB
import net.youapps.transport.components.TripItem
import net.youapps.transport.extensions.displayName
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
    LaunchedEffect(Unit) {
        // set the initial input values if navigated here with already set locations
        originLocationsModel.query.emit(directionsModel.origin.value?.displayName())
        destinationLocationsModel.query.emit(directionsModel.destination.value?.displayName())
    }

    val hasValidLocations by directionsModel.hasValidLocations.collectAsState(false)

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

            // TODO: swap locations button

            LocationSearchBar(
                locationsModel = destinationLocationsModel,
                placeholder = stringResource(R.string.destination),
                leadingIcon = Icons.Default.Flag
            ) {
                scope.launch { directionsModel.destination.emit(it) }
            }
            // TODO: bottom sheet for date, products, ...

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
}