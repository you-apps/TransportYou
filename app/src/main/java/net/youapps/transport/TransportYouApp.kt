package net.youapps.transport

import android.app.Application
import net.youapps.transport.data.NetworkRepository
import net.youapps.transport.data.AppDataRepository

class TransportYouApp: Application() {
    val networkRepository by lazy { NetworkRepository() }
    val appDataRepository by lazy { AppDataRepository(this) }
}