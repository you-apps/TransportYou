package net.youapps.transport.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.DepartureItem
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.RouteRow
import net.youapps.transport.components.TooltipExtendedFAB
import net.youapps.transport.data.toLocation
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.HomeModel
import net.youapps.transport.models.LocationsModel

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
                SmallFloatingActionButton(onClick = {
                    navController.navigate(NavRoutes.Settings)
                }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                }

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
                leadingIcon = Icons.Default.Search
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

                            if (departuresMap[newLocation].isNullOrEmpty()) {
                                homeModel.updateDeparturesForSelectedLocation()
                            }
                        }
                    }

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
                            Text(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                text = listOfNotNull(selectedLocation.name, selectedLocation.place)
                                    .filterNot { it.isEmpty() }
                                    .joinToString(", "),
                                style = MaterialTheme.typography.headlineSmall
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                items(departuresMap[location].orEmpty()) { departure ->
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
                        RouteRow(route.origin.toLocation(), route.destination.toLocation()) {
                            scope.launch {
                                directionsModel.origin.emit(route.origin.toLocation())
                                directionsModel.destination.emit(route.destination.toLocation())
                                navController.navigate(NavRoutes.Directions)
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}