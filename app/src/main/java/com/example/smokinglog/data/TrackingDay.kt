package com.example.smokinglog.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Assigns a real timestamp to the user's waking day without altering the timestamp itself. */
object TrackingDay {
    fun dateFor(timestamp: Long, zone: ZoneId, boundary: LocalTime): LocalDate {
        val local = Instant.ofEpochMilli(timestamp).atZone(zone)
        return if (local.toLocalTime() < boundary) local.toLocalDate().minusDays(1) else local.toLocalDate()
    }

    fun currentDate(now: Instant, zone: ZoneId, boundary: LocalTime): LocalDate =
        dateFor(now.toEpochMilli(), zone, boundary)
}
