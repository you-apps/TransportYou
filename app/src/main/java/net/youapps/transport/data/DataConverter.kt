package net.youapps.transport.data

import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import net.youapps.transport.ProtobufLocation
import net.youapps.transport.ProtobufRoute

fun ProtobufLocation.toLocation(): Location = Location(
    LocationType.valueOf(type),
    id,
    place.takeIf { it.isNotEmpty() },
    name.takeIf { it.isNotEmpty() }
)

fun Location.toProtobufLocation(): ProtobufLocation =
    ProtobufLocation.getDefaultInstance().toBuilder()
        .setType(type.name)
        .setId(id)
        .setName(name.orEmpty())
        .setPlace(place.orEmpty())
        .build()

fun newProtobufRoute(origin: Location, destination: Location): ProtobufRoute =
    ProtobufRoute.getDefaultInstance()
        .toBuilder()
        .setOrigin(origin.toProtobufLocation())
        .setDestination(destination.toProtobufLocation())
        .build()