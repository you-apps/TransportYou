package net.youapps.transport.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import net.youapps.transport.components.generic.SearchBarWithSuggestions
import net.youapps.transport.components.generic.Suggestion
import net.youapps.transport.data.transport.model.Location
import net.youapps.transport.models.LocationsModel

@Composable
fun LocationSearchBar(
    locationsModel: LocationsModel,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable () -> Unit = {},
    onLocation: (Location?) -> Unit
) {
    val scope = rememberCoroutineScope()

    val query by locationsModel.query.collectAsState()
    val suggestions by locationsModel.locationSuggestions.collectAsState(emptyList())

    SearchBarWithSuggestions(
        query = query.orEmpty(),
        onQueryChange = {
            scope.launch { locationsModel.query.emit(it) }
            onLocation(null) // clear location at text change
        },
        searchSuggestions = suggestions.map { location ->
            Suggestion(
                key = location.id!!,
                displayName = location.name
            )
        },
        onSuggestionClicked = { suggestion ->
            val location = suggestions.find { it.id == suggestion.key }
            scope.launch {
                locationsModel.query.emit(location?.name)
            }
            onLocation(location ?: return@SearchBarWithSuggestions)
        },
        leadingIcon = { leadingIcon?.let { Icon(imageVector = it, contentDescription = null) } },
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        onSearch = {}
    )
}