package com.example.smokinglog

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class LogTimeTest {
    private val zone = ZoneId.of("Europe/Lisbon")

    @Test fun `editing time preserves date and seconds`() {
        val original = LocalDateTime.of(2026, 9, 11, 8, 17, 42)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        val edited = Instant.ofEpochMilli(withEditedTime(original, 21, 35, zone)).atZone(zone)

        assertEquals(LocalDateTime.of(2026, 9, 11, 21, 35, 42), edited.toLocalDateTime())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `editing time rejects invalid hour`() {
        withEditedTime(0, 24, 0, zone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `editing time rejects invalid minute`() {
        withEditedTime(0, 12, 60, zone)
    }
}
