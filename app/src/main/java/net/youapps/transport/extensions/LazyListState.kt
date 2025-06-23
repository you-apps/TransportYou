package net.youapps.transport.extensions

import androidx.compose.foundation.lazy.LazyListState

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastScrolledForward && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

internal fun LazyListState.reachedTop(buffer: Int = 0): Boolean {
    val firstVisibleItem = this.layoutInfo.visibleItemsInfo.firstOrNull()
    return lastScrolledBackward && firstVisibleItem?.index == buffer
}