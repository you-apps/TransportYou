package net.youapps.transport.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.youapps.transport.MainActivity
import net.youapps.transport.NavRoutes
import net.youapps.transport.R
import net.youapps.transport.TransportYouApp
import net.youapps.transport.components.directions.DEMO_DEPARTURE
import net.youapps.transport.data.transport.model.Departure
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.data.transport.model.LocationType

class DeparturesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            val scope = rememberCoroutineScope()

            val departures = remember {
                mutableStateListOf<Departure>()
            }
            var isError by rememberSaveable {
                mutableStateOf(false)
            }

            val locationId = currentState(LOCATION_ID_KEY) ?: return@provideContent
            val locationName = currentState(LOCATION_NAME_KEY)

            suspend fun loadDepartures() = withContext(Dispatchers.IO) {
                val app = context.applicationContext as TransportYouApp

                try {
                    val response = app.networkRepository.provider
                        .queryDepartures(Location(locationId, "", LocationType.STATION, null), 20)
                    departures.clear()

                    departures.addAll(response)

                    isError = false
                } catch (e: Exception) {
                    isError = true
                }
            }

            LaunchedEffect(locationId) {
                scope.launch {
                    loadDepartures()
                }
            }

            WidgetContent(
                locationName = locationName.orEmpty(),
                locationId = locationId,
                departures = departures,
                isError = isError,
                onRefresh = {
                    scope.launch { loadDepartures() }
                },
                onConfigureClicked = {
                    val intent =
                        Intent(context, DeparturesWidgetConfigureActivity::class.java)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            )
        }
    }

    @Composable
    private fun WidgetContent(
        locationName: String,
        locationId: String,
        departures: List<Departure>,
        isError: Boolean,
        onRefresh: () -> Unit,
        onConfigureClicked: () -> Unit
    ) {
        val context = LocalContext.current

        val startLocation = remember {
            NavRoutes.DeparturesFromLocation(
                type = LocationType.STATION,
                id = locationId,
                name = locationName,
            )
        }

        Scaffold {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TitleBar(
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .clickable(
                            actionStartActivity(
                                Intent(context, MainActivity::class.java)
                                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    .putExtra(
                                        MainActivity.DEPARTURES_FROM_INTENT_KEY,
                                        startLocation
                                    )
                            )
                        ),
                    startIcon = ImageProvider(R.drawable.ic_app_full_size),
                    title = locationName
                ) {
                    // refresh action
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.outline_refresh_24),
                        backgroundColor = null,
                        contentDescription = null,
                        onClick = onRefresh,
                    )

                    // configuration action
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.outline_settings_24),
                        backgroundColor = null,
                        contentDescription = null,
                        onClick = onConfigureClicked
                    )
                }

                if (isError) {
                    Box(
                        modifier = GlanceModifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = context.getString(R.string.error_fetching_departures),
                            style = TextStyle(
                                color = GlanceTheme.colors.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    LazyColumn {
                        items(departures) { departure ->
                            GlanceDepartureItem(departure) {
                                val intent = Intent(context, MainActivity::class.java)
                                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                    .putExtra(MainActivity.DIRECTIONS_FROM_KEY, startLocation)
                                    .putExtra(
                                        MainActivity.DIRECTIONS_TO_KEY,
                                        NavRoutes.DeparturesFromLocation(
                                            type = LocationType.STATION,
                                            id = departure.destination.id,
                                            name = departure.destination.name,
                                        )
                                    )
                                context.startActivity(intent)
                            }
                        }

                        item {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            WidgetContent(
                locationName = "Frankfurt",
                locationId = "",
                departures = listOf(DEMO_DEPARTURE, DEMO_DEPARTURE, DEMO_DEPARTURE),
                isError = false,
                onRefresh = {},
                onConfigureClicked = {}
            )
        }
    }

    companion object {
        val LOCATION_NAME_KEY = stringPreferencesKey("LOCATION_NAME")
        val LOCATION_ID_KEY = stringPreferencesKey("LOCATION_ID")
    }
}