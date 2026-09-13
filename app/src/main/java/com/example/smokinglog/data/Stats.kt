package com.example.smokinglog.data

import java.time.*
import java.time.format.DateTimeParseException

data class DayTotal(
    val date: LocalDate,
    val amount: Double,
    val smokingEvents: Int,
    val resisted: Int,
)

object Stats {
    fun dayTotals(
        entries: List<LogEntry>,
        days: Int,
        now: Instant,
        zone: ZoneId,
        dayBoundary: LocalTime = LocalTime.MIDNIGHT,
    ): List<DayTotal> {
        val today = TrackingDay.currentDate(now, zone, dayBoundary)
        return rangeTotals(entries, today.minusDays((days - 1).coerceAtLeast(0).toLong()), today, zone, dayBoundary)
    }

    fun rangeTotals(
        entries: List<LogEntry>,
        start: LocalDate,
        endInclusive: LocalDate,
        zone: ZoneId,
        dayBoundary: LocalTime = LocalTime.MIDNIGHT,
    ): List<DayTotal> {
        if (endInclusive < start) return emptyList()
        return generateSequence(start) { current -> current.plusDays(1).takeIf { it <= endInclusive } }.map { date ->
            val matching = entries.filter { TrackingDay.dateFor(it.timestamp, zone, dayBoundary) == date }
            val smoked = matching.filter { it.type == EntryType.SMOKED }
            DayTotal(date, smoked.sumOf { it.amount }, smoked.size,
                matching.count { it.type == EntryType.RESISTED })
        }.toList()
    }

    fun csv(entries: List<LogEntry>, zone: ZoneId): String = buildString {
        appendLine("date,time,type,amount,note,urge_strength,trigger,coping_strategy,urge_duration_minutes,feeling_after")
        entries.sortedBy { it.timestamp }.forEach { entry ->
            val time = Instant.ofEpochMilli(entry.timestamp).atZone(zone)
            fun escaped(value: String) = "\"${value.replace("\"", "\"\"")}\""
            appendLine(listOf(
                time.toLocalDate(), time.toLocalTime().withNano(0), entry.type, entry.amount,
                escaped(entry.note), entry.urgeStrength ?: "", escaped(entry.trigger),
                escaped(entry.copingStrategy), entry.urgeDurationMinutes ?: "", escaped(entry.feelingAfter),
            ).joinToString(","))
        }
    }

    fun parseCsv(csv: String, zone: ZoneId): List<LogEntry> {
        val rows = csvRows(csv.removePrefix("\uFEFF"))
        require(rows.isNotEmpty()) { "The CSV is empty" }
        val legacyHeader = listOf("date", "time", "type", "amount", "note", "urge_strength")
        val currentHeader = legacyHeader + listOf("trigger", "coping_strategy", "urge_duration_minutes", "feeling_after")
        val header = rows.first()
        require(header == legacyHeader || header == currentHeader) {
            "This does not look like a Mostly Harmless export"
        }
        val isCurrent = header == currentHeader
        return rows.drop(1).filterNot { row -> row.all(String::isBlank) }.mapIndexed { index, row ->
            val expectedColumns = if (isCurrent) 10 else 6
            require(row.size == expectedColumns) { "Row ${index + 2} has ${row.size} columns instead of $expectedColumns" }
            try {
                val type = EntryType.valueOf(row[2])
                val amount = row[3].toDouble()
                require(amount.isFinite() && amount >= 0.0) { "amount must be a finite non-negative number" }
                val strength = row[5].takeIf(String::isNotBlank)?.toInt()
                require(strength == null || strength in 1..5) { "urge strength must be between 1 and 5" }
                val duration = row.getOrNull(8)?.takeIf(String::isNotBlank)?.toInt()
                require(duration == null || duration >= 0) { "urge duration must be non-negative" }
                LogEntry(
                    timestamp = LocalDateTime.of(LocalDate.parse(row[0]), LocalTime.parse(row[1]))
                        .atZone(zone).toInstant().toEpochMilli(),
                    type = type,
                    amount = amount,
                    note = row[4],
                    urgeStrength = strength,
                    trigger = row.getOrElse(6) { "" },
                    copingStrategy = row.getOrElse(7) { "" },
                    urgeDurationMinutes = duration,
                    feelingAfter = row.getOrElse(9) { "" },
                )
            } catch (error: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid data on row ${index + 2}: ${error.message}", error)
            } catch (error: DateTimeParseException) {
                throw IllegalArgumentException("Invalid date or time on row ${index + 2}", error)
            }
        }
    }

    private fun csvRows(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0
        while (index < csv.length) {
            val char = csv[index]
            when {
                char == '"' && quoted && csv.getOrNull(index + 1) == '"' -> {
                    field.append('"'); index++
                }
                char == '"' -> quoted = !quoted
                char == ',' && !quoted -> { row.add(field.toString()); field.clear() }
                (char == '\n' || char == '\r') && !quoted -> {
                    if (char == '\r' && csv.getOrNull(index + 1) == '\n') index++
                    row.add(field.toString()); field.clear(); rows.add(row); row = mutableListOf()
                }
                else -> field.append(char)
            }
            index++
        }
        require(!quoted) { "The CSV contains an unfinished quoted field" }
        if (field.isNotEmpty() || row.isNotEmpty()) { row.add(field.toString()); rows.add(row) }
        return rows
    }
}
