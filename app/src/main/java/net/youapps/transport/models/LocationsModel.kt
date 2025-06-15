package net.youapps.transport.models

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import de.schildbach.pte.dto.LocationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.youapps.transport.TransportYouApp
import net.youapps.transport.data.NetworkRepository

class LocationsModel(
    private val networkRepository: NetworkRepository
): ViewModel() {
    val query = MutableStateFlow<String?>(null)

    val locationSuggestions = query.map { query ->
        if (query == null || query.length < 3) return@map emptyList()

        withContext(Dispatchers.IO) {
            try {
                networkRepository.provider.suggestLocations(query, setOf(LocationType.ANY), 10)
                    ?.suggestedLocations?.mapNotNull { it.location }.orEmpty()
            } catch (e: Exception) {
                Log.e("location suggestions", e.toString())
                emptyList()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as TransportYouApp
                return LocationsModel(application.networkRepository) as T
            }
        }
    }
}