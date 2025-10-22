package net.youapps.transport.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.dellisd.spatialk.geojson.LineString
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import io.github.dellisd.spatialk.geojson.dsl.featureCollection
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.directions.TripLegIndividual
import net.youapps.transport.components.directions.TripLegPublic
import net.youapps.transport.components.directions.TripSummary
import net.youapps.transport.components.directions.shouldSkip
import net.youapps.transport.components.generic.RefreshLoadingBox
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
            LazyColumn {
                item {
                    TripSummary(trip)

                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(trip.legs.filterNot { it.shouldSkip() }) { leg ->
                    HorizontalDivider()

                    when (leg) {
                        is TripLeg.Public -> TripLegPublic(leg) { location ->
                            navController.navigate(NavRoutes.DeparturesFromLocation(location))
                        }

                        is TripLeg.Individual -> TripLegIndividual(leg) { location ->
                            navController.navigate(NavRoutes.DeparturesFromLocation(location))
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
        val startCoordinates = trip.legs.first().departure.location.position
        val cameraState = rememberCameraState(
            firstPosition = CameraPosition(
                target = startCoordinates?.let { startCoordinates ->
                    Position(startCoordinates.longitude, startCoordinates.latitude)
                } ?: Position(0.0, 0.0),
                zoom = 10.0
            )
        )

        val mapStyle = if (isSystemInDarkTheme()) "dark" else "liberty"
        MaplibreMap(
            modifier = Modifier.padding(pV),
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/$mapStyle"),
            cameraState = cameraState
        ) {
            val trainStations = remember {
                trip.legs.flatMap { listOf(it.departure.location, it.arrival.location) }.distinct()
            }

            if (trainStations.size >= 2) {
                val firstStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        feature(trainStations.first().geoPosition()?.let { Point(it) }) {
                            put("name", trainStations.first().name)
                        }
                    })
                )
                SymbolLayer(
                    id = "start_station",
                    source = firstStationSource,
                    iconImage = image(painterResource(R.drawable.ic_geo_marker))
                )

                val intermediateStationsSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        trainStations.subList(1, trainStations.size - 1).forEach { station ->
                            feature(station.geoPosition()?.let { Point(it) }) {
                                put("name", station.name)
                            }
                        }
                    })
                )

                SymbolLayer(
                    id = "intermediate_stations",
                    source = intermediateStationsSource,
                    iconImage = image(painterResource(R.drawable.ic_swap)),
                    minZoom = 7f
                )

                val lastStationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(featureCollection {
                        feature(trainStations.last().geoPosition()?.let { Point(it) }) {
                            put("name", trainStations.last().name)
                        }
                    })
                )
                SymbolLayer(
                    id = "end_station",
                    source = lastStationSource,
                    iconImage = image(painterResource(R.drawable.ic_destination))
                )
            }

            val tripSectionsSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(featureCollection {
                    feature(
                        LineString(
                            trip.legs.flatMap { it.path.orEmpty() }.distinct().map {
                                Position(it.longitude, it.latitude)
                            }
                        )
                    )
                })
            )

            LineLayer(
                id = "tripSections",
                source = tripSectionsSource,
                color = const(Color.Red),
                width = const(2.dp)
            )
        }
    }
}

private fun Location.geoPosition() = position?.let { Position(it.longitude, it.latitude) }
private fun de.schildbach.pte.dto.Point.geoPosition() = Position(lonAsDouble, latAsDouble)