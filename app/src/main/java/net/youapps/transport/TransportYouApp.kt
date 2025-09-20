package net.youapps.transport

import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.youapps.transport.data.AppDataRepository
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.widget.DeparturesWidgetReceiver

class TransportYouApp: Application() {
    val networkRepository by lazy { NetworkRepository() }
    val appDataRepository by lazy { AppDataRepository(this) }

    override fun onCreate() {
        super.onCreate()

        // set the network provider automatically on update
        CoroutineScope(Dispatchers.IO).launch {
            appDataRepository.savedNetworkFlow.collect { networkId ->
                networkRepository.updateProvider(networkId)
            }
        }
    }
}