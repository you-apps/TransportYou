package net.youapps.transport.data

import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider

class NetworkRepository {
    private var _provider: NetworkProvider? = null
    val provider get() = _provider!!

    fun updateProvider(networkId: NetworkId) {
        _provider = TransportNetworks.networks.first { it.id == networkId }.factory()
    }
}