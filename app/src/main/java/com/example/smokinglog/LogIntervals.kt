package com.example.smokinglog

import com.example.smokinglog.data.LogEntry

/** Returns elapsed milliseconds from the immediately preceding chronological entry. */
internal fun intervalsSincePrevious(entries: List<LogEntry>): Map<Long, Long?> {
    var previousTimestamp: Long? = null
    return entries
        .sortedWith(compareBy<LogEntry> { it.timestamp }.thenBy { it.id })
        .associate { entry ->
            val interval = previousTimestamp?.let { (entry.timestamp - it).coerceAtLeast(0) }
            previousTimestamp = entry.timestamp
            entry.id to interval
        }
}

internal fun formatInterval(milliseconds: Long): String {
    val totalMinutes = milliseconds.coerceAtLeast(0) / 60_000
    if (totalMinutes == 0L) return "< 1m"

    val days = totalMinutes / (24 * 60)
    val hours = totalMinutes % (24 * 60) / 60
    val minutes = totalMinutes % 60
    return buildList {
        if (days > 0) add("${days}d")
        if (hours > 0) add("${hours}h")
        if (minutes > 0 || isEmpty()) add("${minutes}m")
    }.joinToString(" ")
}
