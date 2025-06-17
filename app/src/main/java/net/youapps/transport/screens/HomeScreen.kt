package net.youapps.transport.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ForkRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.components.TooltipExtendedFAB
import net.youapps.transport.models.LocationsModel

@Composable
fun HomeScreen(navController: NavController, locationsModel: LocationsModel) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationSearchBar(
                locationsModel = locationsModel,
                placeholder = stringResource(R.string.search),
                leadingIcon = Icons.Default.Search
            ) { location ->
                if (location == null) return@LocationSearchBar

                navController.navigate(NavRoutes.DeparturesFromLocation(location))
            }
        }
        // TODO: swipe-able card of departures at saved place
        // TODO: list of saved routes
    }
}