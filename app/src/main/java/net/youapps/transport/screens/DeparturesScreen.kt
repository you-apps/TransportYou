package net.youapps.transport.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.schildbach.pte.dto.Location
import kotlinx.coroutines.launch
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.DepartureItem
import net.youapps.transport.components.TooltipIconButton
import net.youapps.transport.extensions.displayName
import net.youapps.transport.models.DeparturesModel
import net.youapps.transport.models.DirectionsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesScreen(
    navController: NavController,
    departuresModel: DeparturesModel,
    directionsModel: DirectionsModel,
    location: Location
) {
    val scope = rememberCoroutineScope()
    val isLocationSaved by departuresModel.isLocationSaved.collectAsStateWithLifecycle(false)

    LaunchedEffect(Unit) {
        departuresModel.location.emit(location)
        departuresModel.fetchDepartures()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            MediumTopAppBar(
                title = { Text(location.displayName()) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    TooltipIconButton(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    ) {
                        navController.popBackStack()
                    }
                },
                actions = {
                    Row {
                        TooltipIconButton(
                            imageVector = if (isLocationSaved) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = stringResource(R.string.save)
                        ) {
                            if (isLocationSaved) departuresModel.removeSavedLocation()
                            else departuresModel.addSavedLocation()
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val departures by departuresModel.departuresFlow
                .collectAsStateWithLifecycle()

            LazyColumn {
                items(departures.sortedBy { it.plannedTime }) { departure ->
                    DepartureItem(departure) { destination ->
                        scope.launch {
                            directionsModel.origin.emit(location)
                            directionsModel.destination.emit(destination)
                            directionsModel.queryTrips()
                        }
                        navController.navigate(NavRoutes.Directions)
                    }

                    HorizontalDivider()
                }
            }
        }
    }
}