package net.youapps.transport.data

import android.content.Context
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.ProtobufRoute

class AppDataRepository(private val context: Context) {
    val savedLocationsFlow: Flow<List<ProtobufLocation>> = context.appData.data
        .map { appData ->
            // The exampleCounter property is generated from the proto schema.
            appData.savedLocations.locationsList
        }

    val savedRoutesFlow: Flow<List<ProtobufRoute>> = context.appData.data
        .map { appData ->
            appData.savedRoutes.routesList
        }

    val savedProductsFlow: Flow<Set<Product>> = context.appData.data
        .map { appData ->
            appData.savedRouteConfig.productsList.map { Product.valueOf(it) }.toSet()
        }

    val savedNetworkFlow: Flow<NetworkId> = context.appData.data
        .map { appData ->
            appData.savedSettings.networkId.takeIf { it.isNotEmpty() }?.let { NetworkId.valueOf(it) }
                ?: NetworkId.DB
        }

    suspend fun addSavedLocation(location: ProtobufLocation) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .addLocations(location)
                    .build()
            }
                .build()
        }
    }

    suspend fun removeSavedLocation(location: ProtobufLocation) {
        context.appData.updateData { appData ->
            val indexToRemove = appData.savedLocations.locationsList.indexOf(location)

            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .removeLocations(indexToRemove)
                    .build()
            }
                .build()
        }
    }

    suspend fun clearSavedLocations() {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .clearLocations()
                    .build()
            }
                .build()
        }
    }

    suspend fun setSavedLocations(locations: List<ProtobufLocation>) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedLocations = savedLocations.toBuilder()
                    .clearLocations()
                    .addAllLocations(locations)
                    .build()
            }
                .build()
        }
    }

    suspend fun addSavedRoute(route: ProtobufRoute) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedRoutes = savedRoutes.toBuilder()
                    .addRoutes(route)
                    .build()
            }
                .build()
        }
    }

    suspend fun removeSavedRoute(route: ProtobufRoute) {
        context.appData.updateData { appData ->
            val indexToRemove = appData.savedRoutes.routesList.indexOfFirst {
                it.origin.id == route.origin.id && it.destination.id == route.destination.id
            }

            appData.toBuilder().apply {
                savedRoutes = savedRoutes.toBuilder()
                    .removeRoutes(indexToRemove)
                    .build()
            }
                .build()
        }
    }

    suspend fun clearSavedRoutes() {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedRoutes = savedRoutes.toBuilder()
                    .clearRoutes()
                    .build()
            }
                .build()
        }
    }

    suspend fun addProduct(product: Product) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedRouteConfig = savedRouteConfig.toBuilder()
                    .addProducts(product.name)
                    .build()
            }
                .build()
        }
    }

    suspend fun removeProduct(product: Product) {
        context.appData.updateData { appData ->
            val products = appData.savedRouteConfig.productsList - product.name

            appData.toBuilder().apply {
                savedRouteConfig = savedRouteConfig.toBuilder()
                    .clearProducts()
                    .addAllProducts(products)
                    .build()
            }
                .build()
        }
    }

    suspend fun setTransportNetwork(networkId: NetworkId) {
        context.appData.updateData { appData ->
            appData.toBuilder().apply {
                savedSettings = savedSettings.toBuilder()
                    .setNetworkId(networkId.name)
                    .build()
            }
                .build()
        }
    }
}