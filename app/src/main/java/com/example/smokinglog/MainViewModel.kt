package com.example.smokinglog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smokinglog.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as SmokingLogApplication).database.logDao()
    private val settings = SettingsRepository(application)
    val entries = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val dailyTarget = settings.dailyTarget.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun log(amount: Double, onLogged: (LogEntry) -> Unit = {}) = viewModelScope.launch {
        val entry = LogEntry(timestamp = System.currentTimeMillis(), type = EntryType.SMOKED, amount = amount)
        val id = dao.insert(entry)
        onLogged(entry.copy(id = id))
    }
    fun resist(note: String, strength: Int?) = viewModelScope.launch {
        dao.insert(LogEntry(timestamp = System.currentTimeMillis(), type = EntryType.RESISTED,
            note = note.trim(), urgeStrength = strength))
    }
    fun delete(entry: LogEntry) = viewModelScope.launch { dao.delete(entry) }
    fun update(entry: LogEntry) = viewModelScope.launch { dao.update(entry) }
    fun setTarget(value: Double?) = viewModelScope.launch { settings.setDailyTarget(value) }
    fun importCsv(csv: String, zone: ZoneId, onComplete: (Result<Int>) -> Unit) = viewModelScope.launch {
        onComplete(runCatching {
            val imported = Stats.parseCsv(csv, zone)
            dao.replaceAll(imported)
            imported.size
        })
    }
}
