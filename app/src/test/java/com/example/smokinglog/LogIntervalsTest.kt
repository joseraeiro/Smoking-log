package com.example.smokinglog

import com.example.smokinglog.data.EntryType
import com.example.smokinglog.data.LogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogIntervalsTest {
    @Test fun `intervals use chronological order regardless of display order`() {
        val entries = listOf(
            entry(id = 3, timestamp = 10_000_000),
            entry(id = 1, timestamp = 1_000_000),
            entry(id = 2, timestamp = 4_600_000),
        )

        val intervals = intervalsSincePrevious(entries)

        assertNull(intervals.getValue(1))
        assertEquals(3_600_000L, intervals.getValue(2))
        assertEquals(5_400_000L, intervals.getValue(3))
    }

    @Test fun `all event types participate in intervals`() {
        val entries = listOf(
            entry(id = 1, timestamp = 0),
            entry(id = 2, timestamp = 120_000, type = EntryType.RESISTED),
            entry(id = 3, timestamp = 300_000),
        )

        val intervals = intervalsSincePrevious(entries)

        assertEquals(120_000L, intervals.getValue(2))
        assertEquals(180_000L, intervals.getValue(3))
    }

    @Test fun `interval formatter keeps useful day hour and minute precision`() {
        assertEquals("< 1m", formatInterval(59_999))
        assertEquals("1h", formatInterval(3_600_000))
        assertEquals("1d 2h 3m", formatInterval(93_780_000))
    }

    private fun entry(id: Long, timestamp: Long, type: EntryType = EntryType.SMOKED) =
        LogEntry(id = id, timestamp = timestamp, type = type)
}
