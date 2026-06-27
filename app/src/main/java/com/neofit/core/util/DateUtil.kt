package com.neofit.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Date/time helpers built on java.time (available from API 26). */
object DateUtil {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun todayEpochDay(): Long = LocalDate.now(zone).toEpochDay()

    fun nowMillis(): Long = System.currentTimeMillis()

    fun epochDayOf(millis: Long): Long =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().toEpochDay()

    fun startOfDayMillis(epochDay: Long): Long =
        LocalDate.ofEpochDay(epochDay).atStartOfDay(zone).toInstant().toEpochMilli()

    fun endOfDayMillis(epochDay: Long): Long =
        startOfDayMillis(epochDay + 1) - 1

    fun shortDate(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

    fun weekday(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))

    fun timeLabel(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zone)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))

    /** Inclusive list of the last [count] epoch-days ending today (oldest first). */
    fun lastDays(count: Int): List<Long> {
        val today = todayEpochDay()
        return (count - 1 downTo 0).map { today - it }
    }
}
