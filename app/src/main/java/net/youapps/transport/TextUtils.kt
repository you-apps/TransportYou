package net.youapps.transport

import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit
import android.os.Build
import android.text.format.DateUtils
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

fun Date.toZonedDateTime() = this.toInstant().atZone(ZoneId.systemDefault())
fun ZonedDateTime.toJavaDate() = Date(this.toInstant().toEpochMilli())

object TextUtils {
    val dateTimeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private val durationUnits = arrayOf(
        MeasureUnit.SECOND,
        MeasureUnit.MINUTE,
        MeasureUnit.HOUR,
        MeasureUnit.DAY
    )

    fun formatDateTime(time: ZonedDateTime): String = dateTimeFormatter.format(time)

    fun formatDate(time: ZonedDateTime): String = dateFormatter.format(time)

    fun formatTime(time: ZonedDateTime): String = timeFormatter.format(time)

    /**
     * Prettify a given [durationMillis].
     *
     * If [shortUnitName] is set, the format is like "15 mins", otherwise it's like "10 minutes"-
     */
    private fun prettifyDuration(
        durationMillis: Long,
        shortUnitName: Boolean = true,
        valueUnitSeparator: String = "",
        componentSeparator: String = " ",
        maxNumOfComponents: Int = 2,
        includeSeconds: Boolean = false
    ): String {
        val duration = DateUtils.formatElapsedTime(durationMillis.absoluteValue / 1000)

        return duration
            .split(":")
            .reversed()
            .mapIndexed { index, part ->
                if (!includeSeconds && index == 0) return@mapIndexed null

                val unitName = unitDisplayName(durationUnits[index], shortVersion = shortUnitName)
                if (part != "00") "${part}${valueUnitSeparator}${unitName}" else null
            }
            .reversed()
            .dropWhile { it == null }
            .take(maxNumOfComponents)
            .filterNotNull()
            .joinToString(componentSeparator) { it.removePrefix("0") }
    }

    fun prettifyDurationShortText(durationMillis: Long): String {
        return prettifyDuration(
            durationMillis = durationMillis,
            shortUnitName = true,
            valueUnitSeparator = "",
            componentSeparator = " "
        )
    }

    fun prettifyDurationLongText(durationMillis: Long): String {
        return prettifyDuration(
            durationMillis = durationMillis,
            shortUnitName = false,
            valueUnitSeparator = " ",
            componentSeparator = ", "
        )
    }

    private fun unitDisplayName(unit: MeasureUnit, shortVersion: Boolean = false) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val measureFormat = MeasureFormat.getInstance(
                Locale.getDefault(),
                if (shortVersion) MeasureFormat.FormatWidth.NARROW else MeasureFormat.FormatWidth.WIDE
            )
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
        if (distance < 1000) return "${distance}m"

        return "%.1f".format(distance.toFloat() / 1000) + " km"
    }

    private fun formatTimeDiff(planned: ZonedDateTime, actual: ZonedDateTime): String {
        val diffMillis = dateDifferenceMillis(planned, actual)
        var diffMinutes = (diffMillis / 1000 / 60).toString()

        if (!diffMinutes.startsWith("-")) diffMinutes = "+$diffMinutes"
        return diffMinutes
    }

    fun dateDifferenceMillis(start: ZonedDateTime, end: ZonedDateTime): Long {
        return ChronoUnit.MILLIS.between(start, end)
    }

    fun displayDepartureTimeWithDelay(planned: ZonedDateTime?, predicted: ZonedDateTime?): String {
        if (planned != null && predicted != null) {
            val timeDiff = formatTimeDiff(planned, predicted)
            return formatTime(planned) + " ($timeDiff)"
        }

        return (planned ?: predicted)?.let { formatTime(it) }.orEmpty()
    }
}