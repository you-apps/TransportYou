package net.youapps.transport

import android.app.Application
import net.youapps.transport.data.NetworkRepository

class TransportYouApp: Application() {
    val networkRepository by lazy { NetworkRepository() }
}