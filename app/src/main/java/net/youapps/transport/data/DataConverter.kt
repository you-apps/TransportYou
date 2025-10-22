package net.youapps.transport.data

import net.youapps.transport.ProtobufLocation
import net.youapps.transport.ProtobufRoute
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.LocationType

fun ProtobufLocation.toLocation(): Location = Location(
    id =    id,
    name = listOf(name, place).filter { it.isNotEmpty() }.joinToString(", "),
    type = LocationType.valueOf(type),
    position = null
)

fun Location.toProtobufLocation(): ProtobufLocation =
    ProtobufLocation.getDefaultInstance().toBuilder()
        .setType(type.name)
        .setId(id)
        .setName(name)
        .build()

fun newProtobufRoute(origin: Location, destination: Location): ProtobufRoute =
    ProtobufRoute.getDefaultInstance()
        .toBuilder()
        .setOrigin(origin.toProtobufLocation())
        .setDestination(destination.toProtobufLocation())
        .build()