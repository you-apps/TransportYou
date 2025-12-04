package net.youapps.transport.data.transport.model

enum class Product {
    HIGH_SPEED_TRAIN,
    REGIONAL_TRAIN,
    SUBURBAN_TRAIN,
    TRAM,
    SUBWAY,
    BUS,
    CABLECAR,
    FERRY,
    ON_DEMAND
}

data class TransportLine(
    val id: String?,
    val label: String?,
    val destination: Location?,
    val type: Product?,
    val message: String?
)