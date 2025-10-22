package net.youapps.transport.data

import de.schildbach.pte.NetworkId
import net.youapps.transport.data.transport.PTETransportProvider
import net.youapps.transport.data.transport.TransportProvider

class NetworkRepository {
    lateinit var provider: TransportProvider
        private set

    fun updateProvider(networkId: NetworkId) {
        val network = TransportNetworks.networks.first { it.id == networkId }
        provider = PTETransportProvider(network.factory())
    }
}