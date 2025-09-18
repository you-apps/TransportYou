package net.youapps.transport.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.schildbach.pte.dto.Departure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.youapps.transport.R
import net.youapps.transport.TransportYouApp
import java.util.Date

class DeparturesWidget: GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            MyContent()
        }
    }

    @Composable
    private fun MyContent() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val departures = remember {
            mutableStateListOf<Departure>()
        }

        suspend fun loadDepartures() = withContext(Dispatchers.IO) {
            val app = context.applicationContext as TransportYouApp

            try {
                val response = app.networkRepository.provider
                    .queryDepartures("8000068", Date(), 20, true)
                    .stationDepartures.flatMap { it.departures }
                departures.clear()

                departures.addAll(response)
            } catch (e: Exception) {
                TODO("display errors")
            }
        }

        LaunchedEffect(Unit) {
            scope.launch {
                loadDepartures()
            }
        }

        Scaffold {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TitleBar(
                    startIcon = ImageProvider(R.drawable.ic_app_full_size),
                    title = "Train station name"
                ) {
                    // refresh action
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.outline_refresh_24),
                        backgroundColor = null,
                        contentDescription = null,
                        onClick = {
                            scope.launch { loadDepartures() }
                        }
                    )

                    // configuration action
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.outline_settings_24),
                        backgroundColor = null,
                        contentDescription = null,
                        onClick = {
                            scope.launch {
                                TODO("open configuration")
                            }
                        }
                    )
                }

                LazyColumn {
                    items(departures) { departure ->
                        Text(
                            modifier = GlanceModifier.padding(12.dp).clickable {
                                TODO("open departure")
                            },
                            text = departure.destination?.name.toString(),
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground
                            ),
                        )
                    }
                }
            }
        }
    }
}