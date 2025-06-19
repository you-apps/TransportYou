package net.youapps.transport.data

import de.schildbach.pte.NetworkProvider

class NetworkRepository {
    private var _provider: NetworkProvider? = null
    val provider get() = _provider!!

    init {
        // TODO: user preference via data store
        _provider = TransportNetworks.networks[0].factory()
    }
}