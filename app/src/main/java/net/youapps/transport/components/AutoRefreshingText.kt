package net.youapps.transport.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

@Composable
fun AutoRefreshingText(
    refreshDelaySeconds: Int = 5,
    style: TextStyle = TextStyle.Default,
    getText: () -> String
) {
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            text = getText()
            delay(refreshDelaySeconds * 1000L)
        }
    }

    Text(
        text = text,
        style = style
    )
}