package net.youapps.transport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import net.youapps.transport.models.DeparturesModel
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.HomeModel
import net.youapps.transport.models.LocationsModel
import net.youapps.transport.models.SettingsModel
import net.youapps.transport.screens.DeparturesScreen
import net.youapps.transport.screens.DirectionsScreen
import net.youapps.transport.screens.HomeScreen
import net.youapps.transport.screens.SettingsScreen
import net.youapps.transport.screens.TripDetailsScreen
import net.youapps.transport.ui.theme.TransportYouTheme
import androidx.compose.runtime.collectAsState
import androidx.core.content.IntentCompat

class MainActivity : ComponentActivity() {
    val departuresModel: DeparturesModel by viewModels { DeparturesModel.Factory }
    val locationsModel: LocationsModel by viewModels { LocationsModel.Factory }
    val directionsModel: DirectionsModel by viewModels { DirectionsModel.Factory }
    val homeModel: HomeModel by viewModels { HomeModel.Factory }
    val settingsModel: SettingsModel by viewModels { SettingsModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val initialLocationForDepartures =
            IntentCompat.getParcelableExtra(
                intent, DEPARTURES_FROM_INTENT_KEY,
                NavRoutes.DeparturesFromLocation::class.java
            )

        val initialOriginForDirections =
            IntentCompat.getParcelableExtra(
                intent, DIRECTIONS_FROM_KEY,
                NavRoutes.DeparturesFromLocation::class.java
            )

        val initialDestinationForDirections =
            IntentCompat.getParcelableExtra(
                intent, DIRECTIONS_TO_KEY,
                NavRoutes.DeparturesFromLocation::class.java
            )

        setContent {
            TransportYouTheme {
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    if (initialLocationForDepartures != null) {
                        navController.navigate(initialLocationForDepartures)
                    } else if (initialDestinationForDirections != null && initialOriginForDirections != null) {
                        directionsModel.origin.value = initialOriginForDirections.toLocation()
                        directionsModel.destination.value = initialDestinationForDirections.toLocation()
                        navController.navigate(NavRoutes.Directions)
                    }
                }

                NavHost(navController = navController, startDestination = NavRoutes.Home) {
                    composable<NavRoutes.DeparturesFromLocation> { backStackEntry ->
                        val departuresScreen: NavRoutes.DeparturesFromLocation =
                            backStackEntry.toRoute()
                        DeparturesScreen(
                            navController,
                            departuresModel,
                            directionsModel,
                            departuresScreen.toLocation()
                        )
                    }

                    composable<NavRoutes.Directions> {
                        DirectionsScreen(navController, directionsModel)
                    }

                    composable<NavRoutes.TripDetails> { backStackEntry ->
                        val tripDetails: NavRoutes.TripDetails = backStackEntry.toRoute()

                        val tripWrapper = directionsModel.trips.collectAsState().value
                            .find { it.id == tripDetails.tripId }!!
                        TripDetailsScreen(navController, directionsModel, tripWrapper.trip)
                    }

                    composable<NavRoutes.Home> {
                        HomeScreen(navController, homeModel, locationsModel, directionsModel)
                    }

                    composable<NavRoutes.Settings> {
                        SettingsScreen(navController, settingsModel)
                    }
                }
            }
        }
    }

    companion object {
        const val DEPARTURES_FROM_INTENT_KEY = "departures_from"
        const val DIRECTIONS_FROM_KEY = "directions_from"
        const val DIRECTIONS_TO_KEY = "directions_to"
    }
}
