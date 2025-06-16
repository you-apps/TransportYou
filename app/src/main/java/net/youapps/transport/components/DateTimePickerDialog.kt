package net.youapps.transport.components

import androidx.compose.runtime.Composable
import java.util.Date

@Composable
fun DateTimePickerDialog(
    value: Date,
    onDismissRequest: () -> Unit,
    extraDialogContent: @Composable () -> Unit = {},
    onNewValueConfirm: (Date) -> Unit,
) {
    // TODO: implement
}