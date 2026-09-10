package com.example.smokinglog.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val targetKey = doublePreferencesKey("daily_target")
    val dailyTarget = context.dataStore.data.map { it[targetKey] }
    suspend fun setDailyTarget(value: Double?) = context.dataStore.edit {
        if (value == null) it.remove(targetKey) else it[targetKey] = value
    }
}
