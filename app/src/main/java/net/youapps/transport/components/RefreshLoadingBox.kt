package net.youapps.transport.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import net.youapps.transport.R

enum class RefreshLoadingState {
    INACTIVE,
    LOADING,
    ERROR
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RefreshLoadingBox(state: RefreshLoadingState, onRefresh: () -> Unit) {
    var showError by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        // hide the error message after a short delay to show the usual refresh button again
        if (state == RefreshLoadingState.ERROR) {
            showError = true
            delay(3000)
        }

        showError = false
    }

    Box(contentAlignment = Alignment.Center) {
        when {
            state == RefreshLoadingState.LOADING -> LoadingIndicator()

            state == RefreshLoadingState.ERROR && showError -> Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            else -> TooltipIconButton(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.refresh)
            ) {
                onRefresh.invoke()
            }
        }
    }
}