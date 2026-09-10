package com.example.smokinglog.data

import java.time.*

data class DayTotal(
    val date: LocalDate,
    val amount: Double,
    val smokingEvents: Int,
    val resisted: Int,
)

object Stats {
    fun dayTotals(entries: List<LogEntry>, days: Int, now: Instant, zone: ZoneId): List<DayTotal> {
        val today = now.atZone(zone).toLocalDate()
        return rangeTotals(entries, today.minusDays((days - 1).coerceAtLeast(0).toLong()), today, zone)
    }

    fun rangeTotals(entries: List<LogEntry>, start: LocalDate, endInclusive: LocalDate, zone: ZoneId): List<DayTotal> {
        if (endInclusive < start) return emptyList()
        return generateSequence(start) { current -> current.plusDays(1).takeIf { it <= endInclusive } }.map { date ->
            val matching = entries.filter { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date }
            val smoked = matching.filter { it.type == EntryType.SMOKED }
            DayTotal(date, smoked.sumOf { it.amount }, smoked.size,
                matching.count { it.type == EntryType.RESISTED })
        }.toList()
    }

    fun csv(entries: List<LogEntry>, zone: ZoneId): String = buildString {
        appendLine("date,time,type,amount,note,urge_strength")
        entries.sortedBy { it.timestamp }.forEach { entry ->
            val time = Instant.ofEpochMilli(entry.timestamp).atZone(zone)
            fun escaped(value: String) = "\"${value.replace("\"", "\"\"")}\""
            appendLine(listOf(time.toLocalDate(), time.toLocalTime().withNano(0), entry.type,
                entry.amount, escaped(entry.note), entry.urgeStrength ?: "").joinToString(","))
        }
    }
}
