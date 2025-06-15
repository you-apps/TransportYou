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
import kotlinx.coroutines.launch
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.TransportYouApp
import java.util.Date

class DeparturesModel(
    private val networkRepository: NetworkRepository
): ViewModel() {
    private val _departuresFlow = MutableStateFlow<List<Departure>>(emptyList())
    val departuresFlow get() = _departuresFlow.asStateFlow()

    fun fetchDepartures(location: Location) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val departures = networkRepository.provider.queryDepartures(location.id,
                Date(), 30, true)
            _departuresFlow.emit(departures.stationDepartures.flatMap { it.departures }.filterNotNull())
        } catch (e: Exception) {
            Log.e("exc", e.stackTraceToString())
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY]) as TransportYouApp
                return DeparturesModel(application.networkRepository) as T
            }
        }
    }
}