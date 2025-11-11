package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.youapps.transport.R
import net.youapps.transport.TransportYouApp
import net.youapps.transport.components.generic.RefreshLoadingState
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.newProtobufRoute
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.Product
import net.youapps.transport.data.transport.model.Trip
import java.time.ZonedDateTime

class DirectionsModel(
    private val networkRepository: NetworkRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {
    val origin = MutableStateFlow<Location?>(null)
    val destination = MutableStateFlow<Location?>(null)
    val savedRoutes = appDataRepository.savedRoutesFlow

    val hasAnyLocation = MutableStateFlow(false)
    val hasValidLocations = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            combine(origin, destination) { it }
                .distinctUntilChanged()
                .collectLatest { (origin, destination) ->
                    val hasOriginAndDestination = origin != null && destination != null
                    hasValidLocations.emit(hasOriginAndDestination)

                    // automatically query trips when origin or destination changed
                    if (hasOriginAndDestination) queryTrips()

                    hasAnyLocation.emit(origin != null || destination != null)
                }
        }
    }

    val isRouteSaved =
        combine(origin, destination, savedRoutes) { origin, destination, savedRoutes ->
            if (origin == null || destination == null) return@combine false

            savedRoutes.any { it.origin.id == origin.id && it.destination.id == destination.id }
        }

    val date = MutableStateFlow<ZonedDateTime?>(null)
    val isDepartureDate = MutableStateFlow(true)

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips = _trips.asStateFlow()
    private var _tripsLoadingState = MutableStateFlow(RefreshLoadingState.INACTIVE)
    val tripsLoadingState = _tripsLoadingState.asStateFlow()
    private var tripsFirstPageContext: Any? = null
    private var tripsLastPageContext: Any? = null

    val enabledProducts = appDataRepository.savedProductsFlow
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, emptySet())

    fun queryTrips() = viewModelScope.launch(Dispatchers.IO) {
        val (origin, destination) = (origin.value ?: return@launch) to (destination.value
            ?: return@launch)

        tripsFirstPageContext = null
        tripsLastPageContext = null
        _tripsLoadingState.emit(RefreshLoadingState.LOADING)


        val tripsResp = try {
            networkRepository.provider.queryTrips(
                origin = origin, // start
                destination = destination, // end
                departureTime = if (isDepartureDate.value) date.value
                    ?: ZonedDateTime.now() else null, // date
                arrivalTime = if (!isDepartureDate.value) date.value
                    ?: ZonedDateTime.now() else null, // is date departure date?
                products = enabledProducts.value // advanced trip options
            )
        } catch (e: Exception) {
            Log.e("fetching trips", e.stackTraceToString())
            _tripsLoadingState.emit(RefreshLoadingState.ERROR)
            return@launch
        }

        tripsFirstPageContext = tripsResp.prevPagePagination
        tripsLastPageContext = tripsResp.nextPagePagination

        _tripsLoadingState.emit(RefreshLoadingState.INACTIVE)
        _trips.emit(tripsResp.trips)
    }

    fun getMoreTrips(laterTrips: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        val (origin, destination) = (origin.value ?: return@launch) to (destination.value
            ?: return@launch)

        _tripsLoadingState.emit(RefreshLoadingState.LOADING)

        val tripsResp = try {
            networkRepository.provider.queryTrips(
                origin = origin, // start
                destination = destination, // end
                departureTime = if (isDepartureDate.value) date.value
                    ?: ZonedDateTime.now() else null, // date
                arrivalTime = if (!isDepartureDate.value) date.value
                    ?: ZonedDateTime.now() else null, // is date departure date?
                products = enabledProducts.value, // advanced trip options
                prevPagePagination = if (!laterTrips) tripsFirstPageContext else null,
                nextPagePagination = if (laterTrips) tripsLastPageContext else null
            )
        } catch (e: Exception) {
            _tripsLoadingState.emit(RefreshLoadingState.ERROR)
            Log.e("fetching more trips", e.stackTraceToString())
            return@launch
        }

        _tripsLoadingState.emit(RefreshLoadingState.INACTIVE)

        // remove items that would otherwise be duplicated
        val oldTrips = trips.value
            .filter { newTrip -> !tripsResp.trips.any { it.id == newTrip.id } }

        if (laterTrips) {
            tripsLastPageContext = tripsResp.nextPagePagination
            _trips.emit(oldTrips + tripsResp.trips)
        } else {
            tripsFirstPageContext = tripsResp.prevPagePagination
            _trips.emit(tripsResp.trips + oldTrips)
        }
    }

    fun refreshTrip(trip: Trip) = viewModelScope.launch(Dispatchers.IO) {
        val (origin, destination) = (origin.value ?: return@launch) to (destination.value
            ?: return@launch)

        _tripsLoadingState.emit(RefreshLoadingState.LOADING)

        val tripsResp = try {
            networkRepository.provider.queryTrips(
                origin, // start
                destination, // end
                // start request 5 minutes earlier if the time has changed
                trip.legs.first().firstPredictedDepartureTime?.minusMinutes(5), // date
                null,
                enabledProducts.value // advanced trip options
            )
        } catch (e: Exception) {
            Log.e("fetching trips", e.stackTraceToString())
            _tripsLoadingState.emit(RefreshLoadingState.ERROR)
            return@launch
        }

        if (tripsResp.trips.none { it.id == trip.id }) {
            _tripsLoadingState.emit(RefreshLoadingState.ERROR)
        } else {
            val updatedTrips = _trips.value.map { oldTrip ->
                tripsResp.trips.find { it.id == oldTrip.id } ?: oldTrip
            }
            _trips.emit(updatedTrips)
            _tripsLoadingState.emit(RefreshLoadingState.INACTIVE)
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