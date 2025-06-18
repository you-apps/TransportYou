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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.DepartureItem
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.TooltipExtendedFAB
import net.youapps.transport.models.HomeModel
import net.youapps.transport.models.LocationsModel

@Composable
fun HomeScreen(navController: NavController, homeModel: HomeModel, locationsModel: LocationsModel) {
    Scaffold(
        floatingActionButton = {
            TooltipExtendedFAB(
                imageVector = Icons.Default.ForkRight,
                contentDescription = stringResource(R.string.directions)
            ) {
                navController.navigate(NavRoutes.Directions)
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
            val departures by homeModel.departures.collectAsStateWithLifecycle(emptyList())

            LaunchedEffect(savedLocations) {
                if (savedLocations.isNotEmpty()) homeModel.selectedLocation.emit(savedLocations.first())
            }

            selectedLocation?.let { selectedLocation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .weight(1f)
                ) {
                    val pagerState = rememberPagerState(
                        pageCount = { savedLocations.size }
                    )
                    // TODO: cache departures instead of reloading
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collect { page ->
                            homeModel.selectedLocation.emit(savedLocations[page])
                        }
                    }

                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                            .padding(top = 6.dp),
                        state = pagerState
                    ) { page ->
                        val location = savedLocations[page]

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = listOfNotNull(selectedLocation.name, selectedLocation.place)
                                    .filterNot { it.isEmpty() }
                                    .joinToString(", "),
                                style = MaterialTheme.typography.headlineSmall
                            )

                            if (location == selectedLocation) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 12.dp)
                                ) {
                                    items(departures) { departure ->
                                        DepartureItem(departure) { location ->
                                            navController.navigate(
                                                NavRoutes.DeparturesFromLocation(
                                                    location
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TODO: list of saved routes (static)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .weight(1f)
            ) {}
        }
    }
}