package com.example.smokinglog.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingDayTest {
    private val zone = ZoneId.of("UTC")
    private val fourAm = LocalTime.of(4, 0)

    @Test fun `time before boundary belongs to previous tracking day`() {
        assertEquals(LocalDate.of(2026, 9, 12), trackingDate("2026-09-13T02:30:00Z", fourAm))
    }

    @Test fun `time exactly at boundary begins new tracking day`() {
        assertEquals(LocalDate.of(2026, 9, 13), trackingDate("2026-09-13T04:00:00Z", fourAm))
    }

    @Test fun `midnight boundary preserves calendar day behavior`() {
        assertEquals(LocalDate.of(2026, 9, 13), trackingDate("2026-09-13T00:01:00Z", LocalTime.MIDNIGHT))
    }

    @Test fun `boundary crosses month and year safely`() {
        assertEquals(LocalDate.of(2026, 12, 31), trackingDate("2027-01-01T01:00:00Z", fourAm))
    }

    private fun trackingDate(instant: String, boundary: LocalTime) =
        TrackingDay.dateFor(Instant.parse(instant).toEpochMilli(), zone, boundary)
}
