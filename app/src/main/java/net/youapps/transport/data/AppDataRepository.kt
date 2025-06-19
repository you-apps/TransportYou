package net.youapps.transport.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.ProtobufRoute

class AppDataRepository(private val context: Context) {
    val savedLocationsFlow: Flow<List<ProtobufLocation>> = context.appData.data
        .map { appData ->
            // The exampleCounter property is generated from the proto schema.
            appData.savedLocations.locationsList
        }

    val savedRoutesFlow: Flow<List<ProtobufRoute>> = context.appData.data
        .map { appData ->
            appData.savedRoutes.routesList
        }

    suspend fun addSavedLocation(location: ProtobufLocation) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .addLocations(location)
                    .build()
            }
                .build()
        }
    }

    suspend fun removeSavedLocation(location: ProtobufLocation) {
        context.appData.updateData { appData ->
            val indexToRemove = appData.savedLocations.locationsList.indexOf(location)

            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .removeLocations(indexToRemove)
                    .build()
            }
                .build()
        }
    }

    suspend fun addSavedRoute(route: ProtobufRoute) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedRoutes = savedRoutes.toBuilder()
                    .addRoutes(route)
                    .build()
            }
                .build()
        }
    }

    suspend fun removeSavedRoute(route: ProtobufRoute) {
        context.appData.updateData { appData ->
            val indexToRemove = appData.savedRoutes.routesList.indexOfFirst {
                it.origin.id == route.origin.id && it.destination.id == route.destination.id
            }

            appData.toBuilder().apply {
                savedRoutes = savedRoutes.toBuilder()
                    .removeRoutes(indexToRemove)
                    .build()
            }
                .build()
        }
    }
}