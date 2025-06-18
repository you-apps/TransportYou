package net.youapps.transport

import android.app.Application
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.SettingsRepository

class TransportYouApp: Application() {
    val networkRepository by lazy { NetworkRepository() }
    val settingsRepository by lazy { SettingsRepository(this) }
}