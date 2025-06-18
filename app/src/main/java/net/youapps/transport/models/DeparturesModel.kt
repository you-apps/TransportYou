package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.Departure
import de.schildbach.pte.dto.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.SettingsRepository
import java.util.Date

class DeparturesModel(
    private val networkRepository: NetworkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val location = MutableStateFlow<Location?>(null)

    private val savedLocations = settingsRepository.savedLocationsFlow
    val isLocationSaved = combine(location, savedLocations) { location, savedLocations ->
        savedLocations.any { it.id == location?.id }
    }

    private val _departuresFlow = MutableStateFlow<List<Departure>>(emptyList())
    val departuresFlow get() = _departuresFlow.asStateFlow()

    fun fetchDepartures() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        try {
            val departures = networkRepository.provider
                .queryDepartures(location.id, Date(), 30, true)
            _departuresFlow.emit(departures.stationDepartures.flatMap { it.departures }
                .filterNotNull())
        } catch (e: Exception) {
            Log.e("exc", e.stackTraceToString())
        }
    }

    fun addSavedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        val protoLocation = ProtobufLocation.getDefaultInstance().toBuilder()
            .setId(location.id)
            .setName(location.name)
            .build()

        settingsRepository.addSavedLocation(protoLocation)
    }

    fun removeSavedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        val protoLocation = ProtobufLocation.getDefaultInstance().toBuilder()
            .setId(location.id)
            .setName(location.name)
            .build()

        settingsRepository.removeSavedLocation(protoLocation)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY]) as TransportYouApp
                return DeparturesModel(app.networkRepository, app.settingsRepository) as T
            }
        }
    }
}