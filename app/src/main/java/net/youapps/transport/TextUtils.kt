package net.youapps.transport

import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit
import android.os.Build
import android.text.format.DateUtils
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

fun Date.toDateTime() = this.toInstant().atZone(ZoneId.systemDefault())

object TextUtils {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    
    private val durationUnits = arrayOf(
        MeasureUnit.SECOND,
        MeasureUnit.MINUTE,
        MeasureUnit.HOUR,
        MeasureUnit.DAY
    )

    fun formatTime(time: Date): String {
        val dateTime = time.toDateTime()
        return dateFormatter.format(dateTime)
    }

    fun prettifyDuration(durationMillis: Long): String {
        val duration = DateUtils.formatElapsedTime(durationMillis / 1000)
        return duration
            .split(":")
            .reversed()
            .mapIndexed { index, part ->
                val unitName = unitDisplayName(durationUnits[index])
                if (part != "00") "${part}${unitName}" else null
            }
            .takeLast(2)
            .filterNotNull()
            .joinToString(" ") { it.removePrefix("0") }
    }

    private fun unitDisplayName(unit: MeasureUnit) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val measureFormat = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.NARROW)
        measureFormat.getUnitDisplayName(unit)
    } else {
        when (unit) {
            MeasureUnit.DAY -> "day"
            MeasureUnit.HOUR -> "hour"
            MeasureUnit.MINUTE -> "min"
            MeasureUnit.SECOND -> "sec"
            MeasureUnit.MILLISECOND -> "msec"
            else -> throw IllegalArgumentException()
        }
    }

    fun formatDistance(distance: Int): String {
        if (distance < 1000) return "${this}m"

        return "%.1f".format(distance.toFloat() / 1000) + " km"
    }

    private fun formatTimeDiff(planned: Date, actual: Date): String {
        val diffMillis = actual.time - planned.time
        var diffMinutes = (diffMillis / 1000 / 60).toString()

        if (!diffMinutes.startsWith("-")) diffMinutes = "+$diffMinutes"
        return diffMinutes
    }

    fun displayDepartureTimeWithDelay(planned: Date?, predicted: Date?): String {
        if (planned != null && predicted != null) {
            val timeDiff = formatTimeDiff(planned, predicted)
            return formatTime(planned) + " ($timeDiff)"
        }

        return (planned ?: predicted)?.let { formatTime(it) }.orEmpty()
    }
}