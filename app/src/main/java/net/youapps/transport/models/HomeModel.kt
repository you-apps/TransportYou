package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.Departure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.AppDataRepository
import java.util.Date

class HomeModel(
    private val networkRepository: NetworkRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {
    val savedLocations = appDataRepository.savedLocationsFlow
    val savedRoutes = appDataRepository.savedRoutesFlow
    val selectedLocation = MutableStateFlow<ProtobufLocation?>(null)

    private val _departures = MutableStateFlow<Map<ProtobufLocation, List<Departure>>>(emptyMap())
    val departures = _departures.asStateFlow()

    fun updateDeparturesForSelectedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = selectedLocation.value ?: return@launch

        try {
            val departures = networkRepository.provider
                .queryDepartures(location.id, Date(), 10, true)
                .stationDepartures
                .flatMap { it.departures }
                .filterNotNull()

            val departuresMap = _departures.value.toMutableMap()
            departuresMap.put(location, departures)
            _departures.emit(departuresMap)
        } catch (e: Exception) {
            Log.e("exc", e.stackTraceToString())
        }
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