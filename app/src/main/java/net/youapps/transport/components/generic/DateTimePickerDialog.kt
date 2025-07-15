package net.youapps.transport.components.generic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.youapps.transport.R
import net.youapps.transport.TextUtils
import net.youapps.transport.toZonedDateTime
import java.time.ZonedDateTime
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialValue: ZonedDateTime?,
    onDismissRequest: () -> Unit,
    extraDialogContent: @Composable () -> Unit = {},
    onNewValueConfirm: (ZonedDateTime?) -> Unit,
) {
    var isNowSelected by remember {
        mutableStateOf(initialValue == null)
    }
    var dateTime by remember {
        mutableStateOf(initialValue ?: Date().toZonedDateTime())
    }
    val datePickerState = rememberDatePickerState()

    val timePickerState = rememberTimePickerState()

    fun syncStatesWithDateTime() {
        datePickerState.selectedDateMillis = dateTime.toInstant().toEpochMilli()
        timePickerState.hour = dateTime.hour
        timePickerState.minute = dateTime.minute
    }
    LaunchedEffect(Unit) {
        syncStatesWithDateTime()
    }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onNewValueConfirm(dateTime.takeIf { !isNowSelected })
                    onDismissRequest.invoke()
                }
            ) {
                Text(stringResource(R.string.okay))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(text = stringResource(R.string.trip_time))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CardDefaults.shape)
                            .clickable { showDatePicker = true }
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            text = TextUtils.formatDate(dateTime),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CardDefaults.shape)
                            .clickable { showTimePicker = true }
                    ) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            text = TextUtils.formatTime(dateTime),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isNowSelected) {
                        OutlinedButton(
                            onClick = {
                                isNowSelected = true
                                dateTime = Date().toZonedDateTime()
                                syncStatesWithDateTime()
                            }
                        ) {
                            Text(stringResource(R.string.now))
                        }
                    } else {
                        Button(
                            onClick = {
                                dateTime = Date().toZonedDateTime()
                                syncStatesWithDateTime()
                            }
                        ) {
                            Icon(
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                imageVector = Icons.Default.Check, contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.now))
                        }
                    }
                }

                extraDialogContent.invoke()
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isNowSelected = false

                        val date = datePickerState.selectedDateMillis ?: return@TextButton
                        val selectedDate = Date(date).toZonedDateTime()
                        dateTime = dateTime
                            .withYear(selectedDate.year)
                            .withMonth(selectedDate.month.value)
                            .withDayOfYear(selectedDate.dayOfYear)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.okay))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isNowSelected = false

                        dateTime = dateTime
                            .withHour(timePickerState.hour)
                            .withMinute(timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.okay))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {}
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        }
    }
}