package com.example.smokinglog.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter fun toType(value: String) = EntryType.valueOf(value)
    @TypeConverter fun fromType(type: EntryType) = type.name
}

@Database(entities = [LogEntry::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { abstract fun logDao(): LogDao }
