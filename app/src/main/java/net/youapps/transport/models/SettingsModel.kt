package net.youapps.transport.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.TransportNetwork

class SettingsModel(
    private val appDataRepository: AppDataRepository
): ViewModel() {
    val savedNetworkFlow = appDataRepository.savedNetworkFlow

    fun setNetwork(network: TransportNetwork) = viewModelScope.launch(Dispatchers.IO) {
        // the network repository will be automatically updated by this, see [MainActivity.kt]
        appDataRepository.setTransportNetwork(network.id)

        // locations and saved routes depend on the transport network
        appDataRepository.clearSavedRoutes()
        appDataRepository.clearSavedLocations()
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[APPLICATION_KEY]) as TransportYouApp
                return SettingsModel(app.appDataRepository) as T
            }
        }
    }
}