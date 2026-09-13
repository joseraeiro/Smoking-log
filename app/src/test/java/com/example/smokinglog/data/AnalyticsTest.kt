package com.example.smokinglog.data

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsTest {
    private val zone = ZoneId.of("UTC")

    @Test fun `interval summary includes median and distribution`() {
        val entries = listOf(0L, 20L, 80L, 200L).mapIndexed { index, minute ->
            LogEntry(id = index.toLong() + 1, timestamp = minute * 60_000, type = EntryType.SMOKED)
        }

        val result = Analytics.intervals(entries)!!

        assertEquals(60L, result.medianMinutes)
        assertEquals(20L, result.shortestMinutes)
        assertEquals(120L, result.longestMinutes)
        assertEquals(1, result.under30)
        assertEquals(1, result.from60To119)
        assertEquals(1, result.atLeast120)
    }

    @Test fun `hourly events and first last timing are calculated`() {
        val entries = listOf("2026-09-10T08:00:00Z", "2026-09-10T20:00:00Z").mapIndexed { index, value ->
            LogEntry(id = index.toLong() + 1, timestamp = Instant.parse(value).toEpochMilli(), type = EntryType.SMOKED)
        }

        assertEquals(1, Analytics.hourlySmokingEvents(entries, zone)[8])
        assertEquals(8 * 60, Analytics.dailyTiming(entries, zone, LocalTime.MIDNIGHT).averageFirstMinute)
        assertEquals(20 * 60, Analytics.dailyTiming(entries, zone, LocalTime.MIDNIGHT).averageLastMinute)
    }

    @Test fun `first and last timing follows a tracking day across midnight`() {
        val entries = listOf("2026-09-10T22:00:00Z", "2026-09-11T01:00:00Z").mapIndexed { index, value ->
            LogEntry(id = index.toLong() + 1, timestamp = Instant.parse(value).toEpochMilli(), type = EntryType.SMOKED)
        }

        val timing = Analytics.dailyTiming(entries, zone, LocalTime.of(4, 0))

        assertEquals(22 * 60, timing.averageFirstMinute)
        assertEquals(60, timing.averageLastMinute)
    }

    @Test fun `money spent uses cigarette equivalents`() {
        val entries = listOf(
            LogEntry(timestamp = 0, type = EntryType.SMOKED, amount = 1.0),
            LogEntry(timestamp = 1, type = EntryType.SMOKED, amount = .5),
            LogEntry(timestamp = 2, type = EntryType.RESISTED),
        )
        assertEquals(0.75, Analytics.moneySpent(entries, 10.0, 20)!!, 0.0)
    }

    @Test fun `trigger and coping summaries ignore blank context`() {
        val entries = listOf(
            LogEntry(timestamp = 0, type = EntryType.SMOKED, trigger = "Coffee"),
            LogEntry(timestamp = 60_000, type = EntryType.SMOKED, trigger = "Coffee"),
            LogEntry(timestamp = 120_000, type = EntryType.RESISTED, trigger = "Stress", copingStrategy = "Walked"),
            LogEntry(timestamp = 180_000, type = EntryType.RESISTED),
        )

        assertEquals(listOf("Coffee" to 2, "Stress" to 1), Analytics.triggerCounts(entries))
        assertEquals(listOf("Walked" to 1), Analytics.copingCounts(entries))
    }
}
