package net.youapps.transport.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.youapps.transport.ProtobufLocation

class SettingsRepository(private val context: Context) {
    val savedLocationsFlow: Flow<List<ProtobufLocation>> = context.savedLocationsDataStore.data
        .map { savedLocations ->
            // The exampleCounter property is generated from the proto schema.
            savedLocations.locationsList
        }

    suspend fun addSavedLocation(location: ProtobufLocation) {
        context.savedLocationsDataStore.updateData { currentSavedLocations ->
            currentSavedLocations.toBuilder()
                .addLocations(location)
                .build()
        }
    }

    suspend fun removeSavedLocation(location: ProtobufLocation) {
        context.savedLocationsDataStore.updateData { currentSavedLocations ->
            val indexToRemove = currentSavedLocations.locationsList.indexOf(location)

            currentSavedLocations.toBuilder()
                .removeLocations(indexToRemove)
                .build()
        }
    }
}