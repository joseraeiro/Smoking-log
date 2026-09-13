package com.example.smokinglog.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

class SettingsRepository(private val context: Context) {
    private val targetKey = doublePreferencesKey("daily_target")
    private val dayBoundaryKey = intPreferencesKey("day_boundary_minutes")
    private val packPriceKey = doublePreferencesKey("pack_price")
    private val packSizeKey = intPreferencesKey("pack_size")
    private val currencyKey = stringPreferencesKey("currency")
    val dailyTarget = context.dataStore.data.map { it[targetKey] }
    val dayBoundaryMinutes = context.dataStore.data.map { it[dayBoundaryKey] ?: 0 }
    val packPrice = context.dataStore.data.map { it[packPriceKey] }
    val packSize = context.dataStore.data.map { it[packSizeKey] ?: 20 }
    val currency = context.dataStore.data.map { it[currencyKey] ?: "€" }
    suspend fun setDailyTarget(value: Double?) = context.dataStore.edit {
        if (value == null) it.remove(targetKey) else it[targetKey] = value
    }
    suspend fun setDayBoundary(minutes: Int) = context.dataStore.edit {
        require(minutes in 0 until 24 * 60) { "Day boundary must be a valid time" }
        it[dayBoundaryKey] = minutes
    }
    suspend fun setCostSettings(packPrice: Double?, packSize: Int, currency: String) = context.dataStore.edit {
        require(packPrice == null || packPrice > 0) { "Pack price must be positive" }
        require(packSize > 0) { "Pack size must be positive" }
        if (packPrice == null) it.remove(packPriceKey) else it[packPriceKey] = packPrice
        it[packSizeKey] = packSize
        it[currencyKey] = currency.trim().take(4).ifBlank { "€" }
    }
}
