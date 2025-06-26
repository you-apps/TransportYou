package net.youapps.transport.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.youapps.transport.components.directions.EditLocationsSheet
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.directions.DepartureItem
import net.youapps.transport.components.generic.DismissBackground
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.directions.RouteRow
import net.youapps.transport.components.generic.TooltipExtendedFAB
import net.youapps.transport.components.generic.TooltipIconButton
import net.youapps.transport.data.toLocation
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.HomeModel
import net.youapps.transport.models.LocationsModel
import java.util.Date

/**
 * The delay between automatic, periodic refreshes.
 */
const val REFRESH_MILLIS_DELAY = 60 * 1000L

@Composable
fun HomeScreen(
    navController: NavController,
    homeModel: HomeModel,
    locationsModel: LocationsModel,
    directionsModel: DirectionsModel
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                TooltipExtendedFAB(
                    imageVector = Icons.Default.ForkRight,
                    contentDescription = stringResource(R.string.directions)
                ) {
                    navController.navigate(NavRoutes.Directions)
                }
            }
        }
    ) { pV ->
        Column(
            modifier = Modifier.padding(pV),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LocationSearchBar(
                locationsModel = locationsModel,
                placeholder = stringResource(R.string.search),
                leadingIcon = Icons.Default.Search,
                trailingIcon = {
                    TooltipIconButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    ) {
                        navController.navigate(NavRoutes.Settings)
                    }
                }
            ) { location ->
                if (location == null) return@LocationSearchBar

                navController.navigate(NavRoutes.DeparturesFromLocation(location))
            }

            val selectedLocation by homeModel.selectedLocation.collectAsStateWithLifecycle()
            val savedLocations by homeModel.savedLocations.collectAsStateWithLifecycle(emptyList())
            val departuresMap by homeModel.departures.collectAsStateWithLifecycle(emptyMap())

            LaunchedEffect(savedLocations) {
                if (savedLocations.isNotEmpty()) homeModel.selectedLocation.emit(savedLocations.first())
            }

            // refresh the departures at the selected location each second
            // the effect is auto-cancelled once a new location is selected, so this case doesn't need
            // to be handled manually
            LaunchedEffect(selectedLocation) {
                while (true) {
                    delay(REFRESH_MILLIS_DELAY)
                    homeModel.updateDeparturesForSelectedLocation()
                }
            }

            selectedLocation?.let { selectedLocation ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(1f)
                ) {
                    val pagerState = rememberPagerState(
                        pageCount = { savedLocations.size }
                    )
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collect { page ->
                            val newLocation = savedLocations.getOrNull(page) ?: return@collect
                            homeModel.selectedLocation.emit(newLocation)

                            // only refresh if there's no cached entry or the cached entry is outdated
                            val (cachedDate, _) = departuresMap[newLocation]
                                ?: (null to emptyList())
                            if (cachedDate == null || Date().time - cachedDate.time > REFRESH_MILLIS_DELAY) {
                                homeModel.updateDeparturesForSelectedLocation()
                            }
                        }
                    }

                    var showEditLocationsBottomSheet by rememberSaveable { mutableStateOf(false) }
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 6.dp),
                        state = pagerState
                    ) { page ->
                        val location = savedLocations[page]

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    text = listOfNotNull(
                                        selectedLocation.name,
                                        selectedLocation.place
                                    )
                                        .filterNot { it.isEmpty() }
                                        .joinToString(", "),
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                TooltipIconButton(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_locations)
                                ) {
                                    showEditLocationsBottomSheet = true
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                items(departuresMap[location]?.second.orEmpty()) { departure ->
                                    DepartureItem(departure) { destination ->
                                        scope.launch {
                                            directionsModel.origin.emit(location.toLocation())
                                            directionsModel.destination.emit(destination)
                                            navController.navigate(NavRoutes.Directions)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showEditLocationsBottomSheet) {
                        EditLocationsSheet(
                            locations = savedLocations.map { it.toLocation() },
                            onLocationsUpdated = { newLocations ->
                                homeModel.updateSavedLocations(newLocations)
                            }
                        ) {
                            showEditLocationsBottomSheet = false
                        }
                    }
                }
            }

            val savedRoutes by homeModel.savedRoutes.collectAsStateWithLifecycle(emptyList())

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp)
                ) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            text = stringResource(R.string.saved_routes),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    items(savedRoutes) { route ->
                        val dismissBoxState = rememberSwipeToDismissBoxState()

                        SwipeToDismissBox(
                            state = dismissBoxState,
                            enableDismissFromEndToStart = false,
                            backgroundContent = {
                                DismissBackground()
                            },
                            onDismiss = { direction ->
                                dismissBoxState.reset()
                                homeModel.removeSavedRoute(route)
                            }
                        ) {
                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                RouteRow(
                                    route.origin.toLocation(),
                                    route.destination.toLocation()
                                ) {
                                    scope.launch {
                                        directionsModel.origin.emit(route.origin.toLocation())
                                        directionsModel.destination.emit(route.destination.toLocation())
                                        navController.navigate(NavRoutes.Directions)
                                    }
                                }
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}