package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.TripOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository
import java.util.Date

class DirectionsModel(
    private val networkRepository: NetworkRepository
): ViewModel() {
    val origin = MutableStateFlow<Location?>(null)
    val destination = MutableStateFlow<Location?>(null)

    val hasValidLocations = combine(origin, destination) { (origin, destination) ->
        origin != null && destination != null
    }

    val date = MutableStateFlow<Date?>(null)
    val isDepartureDate = MutableStateFlow<Boolean>(true)

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = _trips.asStateFlow()
    private var tripsFirstPageContext: QueryTripsContext? = null
    private var tripsLastPageContext: QueryTripsContext? = null

    val tripOptions = MutableStateFlow<TripOptions>(TripOptions())

    fun queryTrips() = viewModelScope.launch(Dispatchers.IO) {
        tripsFirstPageContext = null
        tripsLastPageContext = null

        val tripsResp = try {
            networkRepository.provider.queryTrips(
                origin.value, // start
                null, // via
                destination.value, // end
                date.value ?: Date(), // date
                isDepartureDate.value, // is date departure date?
                tripOptions.value // advanced trip options
            )
        } catch (e: Exception) {
            Log.e("fetching trips", e.toString())
            return@launch
        }
        tripsFirstPageContext = tripsResp.context
        tripsLastPageContext = tripsResp.context

        _trips.emit(tripsResp.trips.orEmpty())
    }

    fun getMoreTrips(laterTrips: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val context = if (laterTrips) tripsLastPageContext else tripsFirstPageContext

        val tripsResp = try {
            networkRepository.provider.queryMoreTrips(context, laterTrips)
        } catch (e: Exception) {
            Log.e("fetching more trips", e.toString())
            return@launch
        }
        if (laterTrips) {
            tripsLastPageContext = tripsResp.context
            _trips.emit(trips.value + tripsResp.trips)
        } else {
            tripsFirstPageContext = tripsResp.context
            _trips.emit(tripsResp.trips + trips.value)
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[APPLICATION_KEY] as TransportYouApp
                return DirectionsModel(app.networkRepository) as T
            }
        }
    }
}