package com.example.smokinglog.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter fun toType(value: String) = EntryType.valueOf(value)
    @TypeConverter fun fromType(type: EntryType) = type.name
}

@Database(entities = [LogEntry::class], version = 2, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE log_entries ADD COLUMN trigger TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE log_entries ADD COLUMN copingStrategy TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE log_entries ADD COLUMN urgeDurationMinutes INTEGER")
                database.execSQL("ALTER TABLE log_entries ADD COLUMN feelingAfter TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
