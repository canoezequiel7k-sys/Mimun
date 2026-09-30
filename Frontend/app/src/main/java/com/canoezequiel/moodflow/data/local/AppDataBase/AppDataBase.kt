package com.canoezequiel.moodflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity

// Configuracion de la base de datos de Room en SQLite
@Database(
    entities = [MoodEntryEntity::class, JournalEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Retorna la instancia única Singleton de la base de datos SQLite
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodflow_db"
                ).allowMainThreadQueries() // Permite ejecutar consultas directas en desarrollo local
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}