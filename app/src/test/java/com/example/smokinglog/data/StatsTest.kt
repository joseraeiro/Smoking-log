package com.example.smokinglog.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.*

class StatsTest {
    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2026-09-10T12:00:00Z")

    @Test fun `day totals count halves and resisted urges independently`() {
        val entries = listOf(
            LogEntry(timestamp = Instant.parse("2026-09-10T08:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = 1.0),
            LogEntry(timestamp = Instant.parse("2026-09-10T09:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = .5),
            LogEntry(timestamp = Instant.parse("2026-09-10T10:00:00Z").toEpochMilli(), type = EntryType.RESISTED),
        )
        val today = Stats.dayTotals(entries, 7, now, zone).last()
        assertEquals(1.5, today.amount, 0.0)
        assertEquals(2, today.smokingEvents)
        assertEquals(1, today.resisted)
    }

    @Test fun `custom range is inclusive and excludes outside entries`() {
        val entries = listOf(
            LogEntry(timestamp = Instant.parse("2026-09-07T08:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = 1.0),
            LogEntry(timestamp = Instant.parse("2026-09-08T08:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = .5),
            LogEntry(timestamp = Instant.parse("2026-09-09T08:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = 1.0),
            LogEntry(timestamp = Instant.parse("2026-09-10T08:00:00Z").toEpochMilli(), type = EntryType.SMOKED, amount = 1.0),
        )
        val totals = Stats.rangeTotals(entries, LocalDate.parse("2026-09-08"), LocalDate.parse("2026-09-09"), zone)
        assertEquals(2, totals.size)
        assertEquals(1.5, totals.sumOf { it.amount }, 0.0)
        assertEquals(2, totals.sumOf { it.smokingEvents })
    }

    @Test fun `day totals include empty days in chronological order`() {
        val totals = Stats.dayTotals(emptyList(), 3, now, zone)
        assertEquals(listOf(LocalDate.parse("2026-09-08"), LocalDate.parse("2026-09-09"), LocalDate.parse("2026-09-10")), totals.map { it.date })
        assertTrue(totals.all { it.amount == 0.0 })
        assertTrue(totals.all { it.smokingEvents == 0 })
    }

    @Test fun `csv escapes notes and keeps half amounts`() {
        val csv = Stats.csv(listOf(LogEntry(timestamp = now.toEpochMilli(), type = EntryType.SMOKED,
            amount = .5, note = "Tense, then \"fine\"")), zone)
        assertTrue(csv.startsWith("date,time,type,amount,note,urge_strength"))
        assertTrue(csv.contains("0.5,\"Tense, then \"\"fine\"\"\""))
    }

    @Test fun `exported csv round trips notes including commas quotes and newlines`() {
        val original = listOf(
            LogEntry(timestamp = now.toEpochMilli(), type = EntryType.SMOKED, amount = .5,
                note = "Tense, then \"fine\"\nand calmer"),
            LogEntry(timestamp = now.plusSeconds(60).toEpochMilli(), type = EntryType.RESISTED,
                note = "Walked", urgeStrength = 4),
        )
        val parsed = Stats.parseCsv(Stats.csv(original, zone), zone)
        assertEquals(original.map { it.copy(id = 0) }, parsed)
    }

    @Test fun `invalid csv is rejected before it can be imported`() {
        val error = runCatching { Stats.parseCsv("not,a,valid,export", zone) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
