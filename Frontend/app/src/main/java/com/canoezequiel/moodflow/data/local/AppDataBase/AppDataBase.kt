package com.canoezequiel.moodflow.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity

@Database(
    entities = [MoodEntryEntity::class, JournalEntryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migración oficial de versión 1 a 2 agregando la columna deletedAt sin perder datos del usuario
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mood_entries ADD COLUMN deletedAt TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN deletedAt TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moodflow_db"
                ).addMigrations(MIGRATION_1_2)
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}