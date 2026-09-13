package com.example.smokinglog.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.roundToLong

data class IntervalSummary(
    val averageMinutes: Long,
    val medianMinutes: Long,
    val shortestMinutes: Long,
    val longestMinutes: Long,
    val under30: Int,
    val from30To59: Int,
    val from60To119: Int,
    val atLeast120: Int,
)

data class DailyTiming(val averageFirstMinute: Int?, val averageLastMinute: Int?)

object Analytics {
    fun entriesInRange(
        entries: List<LogEntry>,
        start: LocalDate,
        endInclusive: LocalDate,
        zone: ZoneId,
        boundary: LocalTime,
    ) = entries.filter { TrackingDay.dateFor(it.timestamp, zone, boundary) in start..endInclusive }

    fun intervals(entries: List<LogEntry>): IntervalSummary? {
        val gaps = entries.asSequence()
            .filter { it.type == EntryType.SMOKED }
            .map { it.timestamp }
            .sorted()
            .zipWithNext { a, b -> (b - a).coerceAtLeast(0) / 60_000 }
            .toList()
        if (gaps.isEmpty()) return null
        val sorted = gaps.sorted()
        val median = if (sorted.size % 2 == 1) sorted[sorted.size / 2]
        else ((sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0).roundToLong()
        return IntervalSummary(
            averageMinutes = gaps.average().roundToLong(),
            medianMinutes = median,
            shortestMinutes = sorted.first(),
            longestMinutes = sorted.last(),
            under30 = gaps.count { it < 30 },
            from30To59 = gaps.count { it in 30..59 },
            from60To119 = gaps.count { it in 60..119 },
            atLeast120 = gaps.count { it >= 120 },
        )
    }

    fun hourlySmokingEvents(entries: List<LogEntry>, zone: ZoneId): List<Int> =
        (0..23).map { hour -> entries.count {
            it.type == EntryType.SMOKED && Instant.ofEpochMilli(it.timestamp).atZone(zone).hour == hour
        } }

    fun dailyTiming(entries: List<LogEntry>, zone: ZoneId, boundary: LocalTime): DailyTiming {
        val boundaryMinute = boundary.hour * 60 + boundary.minute
        val firstAndLast = entries.filter { it.type == EntryType.SMOKED }
            .groupBy { TrackingDay.dateFor(it.timestamp, zone, boundary) }
            .values.mapNotNull { day ->
                val relativeMinutes = day.map {
                    val local = Instant.ofEpochMilli(it.timestamp).atZone(zone)
                    (local.hour * 60 + local.minute - boundaryMinute + 24 * 60) % (24 * 60)
                }
                if (relativeMinutes.isEmpty()) null else relativeMinutes.min() to relativeMinutes.max()
            }
        return DailyTiming(
            averageFirstMinute = firstAndLast.map { it.first }.averageOrNull()?.let { (it + boundaryMinute) % (24 * 60) },
            averageLastMinute = firstAndLast.map { it.second }.averageOrNull()?.let { (it + boundaryMinute) % (24 * 60) },
        )
    }

    fun triggerCounts(entries: List<LogEntry>) = entries.asSequence()
        .map { it.trigger.trim() }.filter { it.isNotEmpty() }.groupingBy { it }.eachCount()
        .toList().sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

    fun averageIntervalByTrigger(entries: List<LogEntry>): List<Pair<String, Long>> {
        var previousTimestamp: Long? = null
        return entries.filter { it.type == EntryType.SMOKED }.sortedBy { it.timestamp }
            .mapNotNull { entry ->
                val gap = previousTimestamp?.let { (entry.timestamp - it).coerceAtLeast(0) / 60_000 }
                previousTimestamp = entry.timestamp
                entry.trigger.trim().takeIf { it.isNotEmpty() }?.let { trigger -> gap?.let { trigger to it } }
            }
            .groupBy({ it.first }, { it.second })
            .map { (trigger, gaps) -> trigger to gaps.average().roundToLong() }
            .sortedByDescending { it.second }
    }

    fun copingCounts(entries: List<LogEntry>) = entries.asSequence()
        .filter { it.type == EntryType.RESISTED }.map { it.copingStrategy.trim() }
        .filter { it.isNotEmpty() }.groupingBy { it }.eachCount()
        .toList().sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })

    fun moneySpent(entries: List<LogEntry>, packPrice: Double?, packSize: Int): Double? =
        packPrice?.takeIf { it > 0 }?.let { price ->
            entries.filter { it.type == EntryType.SMOKED }.sumOf { it.amount } * price / packSize.coerceAtLeast(1)
        }

    private fun List<Int>.averageOrNull() = takeIf { it.isNotEmpty() }?.average()?.roundToLong()?.toInt()
}
