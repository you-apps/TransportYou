package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.toProtobufLocation
import net.youapps.transport.data.transport.TransportProvider
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.Location

class DeparturesModel(
    private val networkRepository: NetworkRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {
    val location = MutableStateFlow<Location?>(null)

    private val savedLocations = appDataRepository.savedLocationsFlow
    val isLocationSaved = combine(location, savedLocations) { location, savedLocations ->
        savedLocations.any { it.id == location?.id }
    }

    private val _departuresFlow = MutableStateFlow<List<Departure>>(emptyList())
    val departuresFlow get() = _departuresFlow.asStateFlow()

    fun fetchDepartures() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        try {
            val departures = networkRepository.provider
                .queryDepartures(location, 30)
            _departuresFlow.emit(departures)
        } catch (e: Exception) {
            Log.e("exc", e.stackTraceToString())
        }
    }

    fun addSavedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        appDataRepository.addSavedLocation(location.toProtobufLocation())
    }

    fun removeSavedLocation() = viewModelScope.launch(Dispatchers.IO) {
        val location = location.value ?: return@launch

        appDataRepository.removeSavedLocation(location.toProtobufLocation())
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app =
                    checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY]) as TransportYouApp
                return DeparturesModel(app.networkRepository, app.appDataRepository) as T
            }
        }
    }
}