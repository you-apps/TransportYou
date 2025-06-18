package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.SettingsRepository
import java.util.Date

class HomeModel(
    private val networkRepository: NetworkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val savedLocations = settingsRepository.savedLocationsFlow
    val selectedLocation = MutableStateFlow<ProtobufLocation?>(null)

    val departures = selectedLocation.map { location ->
        if (location == null) return@map emptyList()

        return@map withContext(Dispatchers.IO) {
            try {
                val departures = networkRepository.provider
                    .queryDepartures(location.id, Date(), 30, true)
                departures.stationDepartures.flatMap { it.departures }.filterNotNull()
            } catch (e: Exception) {
                Log.e("exc", e.stackTraceToString())
                return@withContext emptyList()
            }
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as TransportYouApp
                return HomeModel(app.networkRepository, app.settingsRepository) as T
            }
        }
    }
}