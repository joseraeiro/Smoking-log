package com.example.smokinglog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LogEntry>>

    @Insert suspend fun insert(entry: LogEntry): Long
    @Insert suspend fun insertAll(entries: List<LogEntry>)
    @Update suspend fun update(entry: LogEntry)
    @Delete suspend fun delete(entry: LogEntry)

    @Query("DELETE FROM log_entries")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entries: List<LogEntry>) {
        deleteAll()
        insertAll(entries)
    }
}
