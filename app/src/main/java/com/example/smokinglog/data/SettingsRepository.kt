package com.example.smokinglog.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val targetKey = doublePreferencesKey("daily_target")
    private val dayBoundaryKey = intPreferencesKey("day_boundary_minutes")
    val dailyTarget = context.dataStore.data.map { it[targetKey] }
    val dayBoundaryMinutes = context.dataStore.data.map { it[dayBoundaryKey] ?: 0 }
    suspend fun setDailyTarget(value: Double?) = context.dataStore.edit {
        if (value == null) it.remove(targetKey) else it[targetKey] = value
    }
    suspend fun setDayBoundary(minutes: Int) = context.dataStore.edit {
        require(minutes in 0 until 24 * 60) { "Day boundary must be a valid time" }
        it[dayBoundaryKey] = minutes
    }
}
