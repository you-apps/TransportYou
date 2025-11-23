package net.youapps.transport.components.directions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.youapps.transport.R
import net.youapps.transport.TextUtils

private const val MINIMUM_CHANGE_INTERVAL_MINUTES = 3

@Composable
fun TripTransfer(
    changeTimeMillis: Long?,
    distanceMeters: Int? = null,
    walkDurationApproxMillis: Long? = null
) {
    val isChangePossible = changeTimeMillis != null && changeTimeMillis > 0
    val isChangeShort =
        isChangePossible && (changeTimeMillis < MINIMUM_CHANGE_INTERVAL_MINUTES * 1000 || changeTimeMillis < (walkDurationApproxMillis
            ?: 0))
    val changeColor =
        if (isChangePossible && !isChangeShort) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (distanceMeters != null) Icons.AutoMirrored.Filled.DirectionsWalk
            else Icons.AutoMirrored.Filled.CompareArrows,
            contentDescription = stringResource(R.string.transfer),
            tint = changeColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            val changeText =
                if (isChangePossible) stringResource(R.string.transfer)
                else stringResource(R.string.transfer_impossible)
            Text(
                text = changeText + changeTimeMillis?.takeIf { isChangePossible }?.let {
                    " (${TextUtils.prettifyDurationLongText(it)})"
                }
                    .orEmpty(),
                color = changeColor
            )

            distanceMeters?.let { distanceMeters ->
                Text(
                    text = TextUtils.formatDistance(distanceMeters) + walkDurationApproxMillis?.let {
                        ", ${stringResource(R.string.approximately_abbr)} ${
                            TextUtils.prettifyDurationShortText(
                                it
                            )
                        }"
                    },
                    color = changeColor
                )
            }
        }
    }
}