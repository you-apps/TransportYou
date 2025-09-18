package net.youapps.transport.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
internal fun LazyListState.loadPrevItems(): State<Boolean> {
    var currentState by remember {
        // 0: default, 1: top reached, 2: scroll gesture that caused top reached finished
        mutableIntStateOf(0)
    }

    val triggerLoadPrevItems = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isScrollInProgress) {
        val isOnTop = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
        this@loadPrevItems.canScrollForward
        when (currentState) {
            0 -> {
                if (isOnTop) currentState = 1
                triggerLoadPrevItems.value = false
            }
            1 -> {
                if (!isScrollInProgress) currentState = 2
                triggerLoadPrevItems.value = false
            }
            2 -> {
                currentState = 1
                triggerLoadPrevItems.value = isOnTop
            }
        }
    }

    return triggerLoadPrevItems
}