package net.youapps.transport.models

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.TripOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.youapps.transport.R
import net.youapps.transport.TransportYouApp
import net.youapps.transport.components.RefreshLoadingState
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.newProtobufRoute
import java.util.Date

class DirectionsModel(
    private val networkRepository: NetworkRepository,
    private val appDataRepository: AppDataRepository
): ViewModel() {
    val origin = MutableStateFlow<Location?>(null)
    val destination = MutableStateFlow<Location?>(null)
    val savedRoutes = appDataRepository.savedRoutesFlow

    val hasAnyLocation = combine(origin, destination) { (origin, destination) ->
        origin != null || destination != null
    }

    val hasValidLocations = combine(origin, destination) { (origin, destination) ->
        origin != null && destination != null
    }

    val isRouteSaved = combine(origin, destination, savedRoutes) { origin, destination, savedRoutes ->
        if (origin == null || destination == null) return@combine false

        savedRoutes.any { it.origin.id == origin.id && it.destination.id == destination.id }
    }

    val date = MutableStateFlow<Date?>(null)
    val isDepartureDate = MutableStateFlow<Boolean>(true)

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = _trips.asStateFlow()
    private var _tripsLoadingState = MutableStateFlow<RefreshLoadingState>(RefreshLoadingState.INACTIVE)
    val tripsLoadingState = _tripsLoadingState.asStateFlow()
    private var tripsFirstPageContext: QueryTripsContext? = null
    private var tripsLastPageContext: QueryTripsContext? = null

    val enabledProducts = appDataRepository.savedProductsFlow
        .stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), emptySet())

    private val tripOptions get() = TripOptions(
        enabledProducts.value, // products
        null, // optimize
        null, // walk speed
        null, // accessibility
        null, // flags
    )

    fun queryTrips() = viewModelScope.launch(Dispatchers.IO) {
        tripsFirstPageContext = null
        tripsLastPageContext = null
        _tripsLoadingState.emit(RefreshLoadingState.LOADING)

        val tripsResp = try {
            networkRepository.provider.queryTrips(
                origin.value, // start
                null, // via
                destination.value, // end
                date.value ?: Date(), // date
                isDepartureDate.value, // is date departure date?
                tripOptions // advanced trip options
            )
        } catch (e: Exception) {
            Log.e("fetching trips", e.toString())
            _tripsLoadingState.emit(RefreshLoadingState.ERROR)
            return@launch
        }

        _tripsLoadingState.emit(RefreshLoadingState.INACTIVE)
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

    fun swapOriginAndDestination() {
        val temp = origin.value
        origin.value = destination.value
        destination.value = temp
    }

    fun addSavedRoute() = viewModelScope.launch(Dispatchers.IO) {
        val origin = origin.value ?: return@launch
        val location = destination.value ?: return@launch

        val route = newProtobufRoute(origin, location)
        appDataRepository.addSavedRoute(route)
    }

    fun removeSavedRoute() = viewModelScope.launch(Dispatchers.IO) {
        val origin = origin.value ?: return@launch
        val location = destination.value ?: return@launch

        val route = newProtobufRoute(origin, location)
        appDataRepository.removeSavedRoute(route)
    }

    fun addProductType(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        appDataRepository.addProduct(product)
    }

    fun removeProductType(product: Product) = viewModelScope.launch(Dispatchers.IO) {
        appDataRepository.removeProduct(product)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as TransportYouApp
                return DirectionsModel(app.networkRepository, app.appDataRepository) as T
            }
        }

        val productTypes = mapOf(
            Product.HIGH_SPEED_TRAIN to R.string.product_high_speed_train,
            Product.REGIONAL_TRAIN to R.string.product_regional_train,
            Product.SUBURBAN_TRAIN to R.string.product_suburban_train,
            Product.TRAM to R.string.product_tram,
            Product.SUBWAY to R.string.product_subway,
            Product.BUS to R.string.product_bus,
            Product.CABLECAR to R.string.product_cablecar,
            Product.FERRY to R.string.product_ferry,
            Product.ON_DEMAND to R.string.product_on_demand
        )
    }
}