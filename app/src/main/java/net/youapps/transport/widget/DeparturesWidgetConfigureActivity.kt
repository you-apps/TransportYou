package net.youapps.transport.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import de.schildbach.pte.dto.Location
import kotlinx.coroutines.launch
import net.youapps.transport.R
import net.youapps.transport.components.LocationSearchBar
import net.youapps.transport.models.LocationsModel
import net.youapps.transport.ui.theme.TransportYouTheme

class DeparturesWidgetConfigureActivity : ComponentActivity() {
    private val locationsModel: LocationsModel by viewModels { LocationsModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val scope = rememberCoroutineScope()

            var location by remember {
                mutableStateOf<Location?>(null)
            }

            TransportYouTheme {
                Scaffold(
                    floatingActionButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = {
                                finish()
                            }) {
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                enabled = location != null,
                                onClick = {
                                    scope.launch {
                                        installAppWidget(
                                            this@DeparturesWidgetConfigureActivity,
                                            location!!
                                        )
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.okay))
                            }
                        }
                    }
                ) { pV ->
                    Box(
                        modifier = Modifier.padding(pV)
                    ) {
                        LocationSearchBar(
                            locationsModel = locationsModel,
                            placeholder = stringResource(R.string.origin),
                            leadingIcon = Icons.Default.LocationOn
                        ) {
                            location = it
                        }
                    }
                }
            }
        }
    }

    private suspend fun installAppWidget(context: Context, location: Location) {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[DeparturesWidget.LOCATION_NAME_KEY] = location.uniqueShortName()
            prefs[DeparturesWidget.LOCATION_ID_KEY] = location.id!!
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val resultIntent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, resultIntent)

        DeparturesWidget().update(context, glanceId)
        finish()
    }
}