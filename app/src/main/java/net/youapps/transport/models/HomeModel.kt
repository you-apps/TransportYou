package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.Departure
import de.schildbach.pte.dto.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.ProtobufRoute
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.toProtobufLocation
import java.util.Date

class HomeModel(
    private val networkRepository: NetworkRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {
    val savedLocations = appDataRepository.savedLocationsFlow
    val savedRoutes = appDataRepository.savedRoutesFlow
    val selectedLocation = MutableStateFlow<ProtobufLocation?>(null)

    private val _departures = MutableStateFlow<Map<ProtobufLocation, Pair<Date, List<Departure>>>>(emptyMap())
    val departures = _departures.asStateFlow()

    fun updateDeparturesForSelectedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = selectedLocation.value ?: return@launch

        try {
            val requestDate = Date()
            val departures = networkRepository.provider
                .queryDepartures(location.id, requestDate, 15, true)
                .stationDepartures
                .flatMap { it.departures }
                .filterNotNull()

            val departuresMap = _departures.value.toMutableMap()
            departuresMap.put(location, requestDate to departures)
            _departures.emit(departuresMap)
        } catch (e: Exception) {
            Log.e("exc", e.stackTraceToString())
        }
    }

    fun removeSavedRoute(route: ProtobufRoute) = viewModelScope.launch(Dispatchers.IO) {
        appDataRepository.removeSavedRoute(route)
    }

    fun updateSavedLocations(locations: List<Location>) = viewModelScope.launch(Dispatchers.IO) {
        appDataRepository.setSavedLocations(locations.map { it.toProtobufLocation() })
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as TransportYouApp
                return HomeModel(app.networkRepository, app.appDataRepository) as T
            }
        }
    }
}