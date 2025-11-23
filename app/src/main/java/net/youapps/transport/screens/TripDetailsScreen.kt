package net.youapps.transport.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import io.github.dellisd.spatialk.geojson.dsl.featureCollection
import net.youapps.transport.MapLibreUtils
import net.youapps.transport.MapLibreUtils.geoPosition
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.TextUtils
import net.youapps.transport.components.directions.TripLegIndividual
import net.youapps.transport.components.directions.TripLegPublic
import net.youapps.transport.components.directions.TripSummary
import net.youapps.transport.components.directions.TripTransfer
import net.youapps.transport.components.generic.RefreshLoadingBox
import net.youapps.transport.data.transport.model.IndividualType
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.Trip
import net.youapps.transport.data.transport.model.TripLeg
import net.youapps.transport.models.DirectionsModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    navController: NavController,
    directionsModel: DirectionsModel,
    trip: Trip,
) {
    val refreshLoadingState by directionsModel.tripsLoadingState.collectAsState()

    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Expanded
            )
        ),
        sheetContent = {
            val tripLegs = trip.legs
            
            LazyColumn {
                item {
                    TripSummary(trip)

                    Spacer(modifier = Modifier.height(10.dp))
                }

                itemsIndexed(tripLegs) { index, leg ->
                    HorizontalDivider()

                    when (leg) {
                        is TripLeg.Public -> {
                            TripLegPublic(leg) { location ->
                                navController.navigate(NavRoutes.DeparturesFromLocation(location))
                            }

                            // display change time between current and next trip leg
                            tripLegs.getOrNull(index + 1)?.let { nextLeg ->
                                if (nextLeg is TripLeg.Public) {
                                    HorizontalDivider()

                                    val changeTimeMillis = remember {
                                        TextUtils.dateDifferenceMillis(
                                            leg.arrival.arrivalTime.predictedOrPlanned
                                                ?: return@remember null,
                                            nextLeg.departure.departureTime.predictedOrPlanned
                                                ?: return@remember null
                                        )
                                    }

                                    TripTransfer(changeTimeMillis)
                                }
                            }
                        }

                        is TripLeg.Individual -> {
                            val isTransferLeg =
                                leg.type !in arrayOf(IndividualType.BIKE, IndividualType.CAR)
                            if (!isTransferLeg) {
                                TripLegIndividual(leg) { location ->
                                    navController.navigate(NavRoutes.DeparturesFromLocation(location))
                                }
                            } else {
                                // display change time between current and next trip leg
                                tripLegs.getOrNull(index + 1)?.let { nextLeg ->
                                    HorizontalDivider()

                                    val changeTimeMillis = remember {
                                        TextUtils.dateDifferenceMillis(
                                            leg.arrival.arrivalTime.predictedOrPlanned
                                                ?: return@remember null,
                                            nextLeg.departure.departureTime.predictedOrPlanned
                                                ?: return@remember null
                                        )
                                    }

                                    TripTransfer(changeTimeMillis, leg.distance, leg.durationMillis)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        },
        sheetDragHandle = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // approx size of refresh loading box at other side, to make things symmetric
                Spacer(modifier = Modifier.width(40.dp))

                BottomSheetDefaults.DragHandle()

                RefreshLoadingBox(refreshLoadingState) {
                    directionsModel.refreshTrip(trip)
                }
            }
        }
    ) { pV ->
        var selectedStation by remember {
            mutableStateOf<Location?>(null)
        }

        val startCoordinates = trip.legs.first().departure.location.position
        val cameraState = rememberCameraState(
            firstPosition = CameraPosition(
                target = startCoordinates?.let { startCoordinates ->
                    Position(startCoordinates.longitude, startCoordinates.latitude)
                } ?: Position(0.0, 0.0),
                zoom = 10.0
            )
        )

        MaplibreMap(
            modifier = Modifier.padding(pV),
            baseStyle = BaseStyle.Uri(MapLibreUtils.getMapLibreStyleUrl(isSystemInDarkTheme())),
            cameraState = cameraState
        ) {
            val trainStations = remember {
                trip.legs.flatMap { listOf(it.departure.location, it.arrival.location) }.distinct()
            }

            if (trainStations.size >= 2) {
                val firstStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        feature(
                            trainStations.first().geoPosition()?.let { Point(it) },
                            id = trainStations.first().id
                        )
                    })
                )
                SymbolLayer(
                    id = "start_station",
                    source = firstStationSource,
                    iconImage = image(painterResource(R.drawable.ic_geo_marker)),
                    onClick = { features ->
                        selectedStation = trainStations.find { it.id == features.first().id }
                        ClickResult.Consume
                    }
                )

                val intermediateStationsSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        trainStations.subList(1, trainStations.size - 1).forEach { station ->
                            feature(station.geoPosition()?.let { Point(it) }, id = station.id)
                        }
                    })
                )

                SymbolLayer(
                    id = "intermediate_stations",
                    source = intermediateStationsSource,
                    iconImage = image(painterResource(R.drawable.ic_swap)),
                    minZoom = 7f,
                    onClick = { features ->
                        selectedStation = trainStations.find { it.id == features.first().id }
                        ClickResult.Consume
                    }
                )

                val lastStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        feature(
                            trainStations.last().geoPosition()?.let { Point(it) },
                            id = trainStations.last().id
                        )
                    })
                )
                SymbolLayer(
                    id = "end_station",
                    source = lastStationSource,
                    iconImage = image(painterResource(R.drawable.ic_destination)),
                    onClick = { features ->
                        selectedStation = trainStations.find { it.id == features.first().id }
                        ClickResult.Consume
                    }
                )
            }

            trip.legs.forEachIndexed { index, leg ->
                val isPublic = leg is TripLeg.Public

                val tripSectionsPublicSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        feature(
                            LineString(
                                leg.path.orEmpty().map {
                                    Position(it.longitude, it.latitude)
                                }
                            )
                        )
                    })
                )
                LineLayer(
                    // ids must be unique
                    id = "tripSections_$index",
                    source = tripSectionsPublicSource,
                    color = const(if (isPublic) Color.Red else Color.Yellow),
                    width = const(2.dp)
                )
            }
        }

        selectedStation?.let {
            AlertDialog(
                onDismissRequest = {
                    selectedStation = null
                },
                title = {
                    Text(it.name)
                },
                text = {
                    Column {
                        val facts = remember {
                            val arrivalTime =
                                trip.legs.firstOrNull { leg -> leg is TripLeg.Public && leg.arrival.location.id == it.id }
                                    ?.arrival?.arrivalTime?.predictedOrPlanned

                            val departureTime =
                                trip.legs.firstOrNull { leg -> leg is TripLeg.Public && leg.departure.location.id == it.id }
                                    ?.departure?.departureTime?.predictedOrPlanned

                            val timeDiff = if (departureTime != null && arrivalTime != null)
                                TextUtils.dateDifferenceMillis(arrivalTime, departureTime)
                            else null

                            listOf(
                                R.string.longitude to it.position?.longitude?.toString(),
                                R.string.latitude to it.position?.latitude?.toString(),
                                R.string.arrival to arrivalTime?.let { time -> TextUtils.formatTime(time) },
                                R.string.departure to departureTime?.let { time -> TextUtils.formatTime(time) },
                                R.string.length_of_stay to timeDiff?.let { timeDiff -> TextUtils.prettifyDurationLongText(timeDiff) }
                            ).filter { (_, value) -> value != null }
                        }

                        facts.forEach { (strRes, fact) ->
                            Row {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = stringResource(strRes),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = fact!!
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            navController.navigate(NavRoutes.DeparturesFromLocation(it))
                            selectedStation = null
                        }
                    ) {
                        Text(stringResource(R.string.view_departures))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            selectedStation = null
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
