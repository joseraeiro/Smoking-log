package com.example.smokinglog

import android.app.Application
import androidx.room.Room
import com.example.smokinglog.data.AppDatabase

class SmokingLogApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "smoking-log.db").build()
    }
}
