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
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Trip
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
                        is Trip.Public -> TripLegPublic(leg) { location ->
                            navController.navigate(NavRoutes.DeparturesFromLocation(location))
                        }

                        is Trip.Individual -> TripLegIndividual(leg) { location ->
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
        val startCoordinates = trip.legs[0].departure.coord
        val cameraState = rememberCameraState(
            firstPosition = CameraPosition(
                target = startCoordinates?.let { startCoordinates ->
                    Position(startCoordinates.lonAsDouble, startCoordinates.latAsDouble)
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
                trip.legs.flatMap { listOf(it.departure, it.arrival) }.distinct()
            }
            val trainStationsSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(featureCollection {
                    trainStations.forEach { station ->
                        feature(Point(station.geoPosition())) {
                            put("name", station.uniqueShortName())
                        }
                    }
                })
            )

            SymbolLayer(
                id = "stations",
                source = trainStationsSource,
                iconImage = image(painterResource(R.drawable.ic_geo_marker))
            )

            val tripSectionsSource = rememberGeoJsonSource(
                data = GeoJsonData.Features(featureCollection {
                    feature(
                        LineString(
                            trip.legs.flatMap { it.toCoordinateList() }.distinct()
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

private fun Location.geoPosition() = Position(lonAsDouble, latAsDouble)
private fun de.schildbach.pte.dto.Point.geoPosition() = Position(lonAsDouble, latAsDouble)
private fun Trip.Leg.toCoordinateList(): List<Position> {
    return if (!this.path.isNullOrEmpty()) {
        path.map { it.geoPosition() }
    } else if (this is Trip.Public) {
        (listOf(departure) + intermediateStops.orEmpty().map { it.location } + listOf(arrival))
            .filterNotNull()
            .map { it.geoPosition() }
    } else if (departure != null && arrival != null) {
        listOf(departure.geoPosition(), arrival.geoPosition())
    } else {
        emptyList()
    }
}