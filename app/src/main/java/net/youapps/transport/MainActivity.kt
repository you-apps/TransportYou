package net.youapps.transport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import net.youapps.transport.models.DeparturesModel
import net.youapps.transport.models.DirectionsModel
import net.youapps.transport.models.HomeModel
import net.youapps.transport.models.LocationsModel
import net.youapps.transport.screens.DeparturesScreen
import net.youapps.transport.screens.DirectionsScreen
import net.youapps.transport.screens.HomeScreen
import net.youapps.transport.ui.theme.TransportYouTheme

class MainActivity : ComponentActivity() {
    val departuresModel: DeparturesModel by viewModels { DeparturesModel.Factory }
    val locationsModel: LocationsModel by viewModels { LocationsModel.Factory }
    val directionsModel: DirectionsModel by viewModels { DirectionsModel.Factory }
    val homeModel: HomeModel by viewModels { HomeModel.Factory }

    // TODO: use https://github.com/maplibre/maplibre-compose

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TransportYouTheme {
                val navController = rememberNavController()

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

                    composable<NavRoutes.Home> {
                        HomeScreen(navController, homeModel, locationsModel, directionsModel)
                    }
                }
            }
        }
    }
}
