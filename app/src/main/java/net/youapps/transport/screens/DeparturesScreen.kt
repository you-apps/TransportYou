package net.youapps.transport.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.dellisd.spatialk.geojson.Point
import io.github.dellisd.spatialk.geojson.Position
import io.github.dellisd.spatialk.geojson.dsl.featureCollection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.youapps.transport.MapLibreUtils
import net.youapps.transport.MapLibreUtils.geoPosition
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.components.directions.DepartureItem
import net.youapps.transport.components.directions.TransportLineCard
import net.youapps.transport.components.generic.TooltipIconButton
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.models.DeparturesModel
import net.youapps.transport.models.DirectionsModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeparturesScreen(
    navController: NavController,
    departuresModel: DeparturesModel,
    directionsModel: DirectionsModel,
    location: Location
) {
    val scope = rememberCoroutineScope()
    val isLocationSaved by departuresModel.isLocationSaved.collectAsStateWithLifecycle(false)

    LaunchedEffect(location) {
        departuresModel.location.emit(location)
        departuresModel.fetchDepartures()

        // periodically refresh the list of departures
        while (true) {
            delay(REFRESH_MILLIS_DELAY)
            departuresModel.fetchDepartures()
        }
    }

    val bottomSheetBehavior = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded
    )
    BottomSheetScaffold(
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = bottomSheetBehavior
        ),
        sheetContent = {
            Column {
                val lines by departuresModel.linesFlow.collectAsStateWithLifecycle()
                val departures by departuresModel.departuresFlow
                    .collectAsStateWithLifecycle()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    TooltipIconButton(
                        imageVector = if (isLocationSaved) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(R.string.save)
                    ) {
                        if (isLocationSaved) departuresModel.removeSavedLocation()
                        else departuresModel.addSavedLocation()
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(lines.sortedBy { it.type }) {
                        TransportLineCard(it)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(departures.sortedBy { it.departure.planned }) { departure ->
                        DepartureItem(departure) { destination ->
                            scope.launch {
                                directionsModel.origin.emit(location)
                                directionsModel.destination.emit(destination)
                                directionsModel.queryTrips()
                            }
                            navController.navigate(NavRoutes.Directions)
                        }

                        HorizontalDivider()
                    }
                }
            }
        },
        content = { pV ->
            val cameraState = rememberCameraState(
                firstPosition = CameraPosition(
                    target = location.geoPosition() ?: Position(0.0, 0.0),
                    zoom = 10.0
                )
            )

            MaplibreMap(
                modifier = Modifier.padding(pV),
                baseStyle = BaseStyle.Uri(MapLibreUtils.getMapLibreStyleUrl(isSystemInDarkTheme())),
                cameraState = cameraState
            ) {
                val locationSource = rememberGeoJsonSource(
                    data = GeoJsonData.Features(
                        featureCollection {
                            feature(location.geoPosition()?.let { Point(it) }) {
                                put("name", location.name)
                            }
                        }
                    )
                )

                SymbolLayer(
                    id = "location",
                    source = locationSource,
                    iconImage = image(painterResource(R.drawable.ic_geo_marker)),
                    onClick = { _ ->
                        scope.launch {
                            bottomSheetBehavior.expand()
                        }
                        ClickResult.Consume
                    }
                )
            }
        }
    )
}