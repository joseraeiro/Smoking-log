package com.example.smokinglog.data

import java.time.*

data class DayTotal(val date: LocalDate, val amount: Double, val resisted: Int)

object Stats {
    fun dayTotals(entries: List<LogEntry>, days: Int, now: Instant, zone: ZoneId): List<DayTotal> {
        val today = now.atZone(zone).toLocalDate()
        return (days - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val matching = entries.filter { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() == date }
            DayTotal(date, matching.filter { it.type == EntryType.SMOKED }.sumOf { it.amount },
                matching.count { it.type == EntryType.RESISTED })
        }
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
