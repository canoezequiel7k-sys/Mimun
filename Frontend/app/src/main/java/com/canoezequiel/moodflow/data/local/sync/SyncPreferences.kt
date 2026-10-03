package com.canoezequiel.moodflow.data.local.sync

import android.content.Context

class SyncPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    fun getLastMoodCursor(): String? = prefs.getString("last_mood_cursor", null)
    fun saveLastMoodCursor(cursor: String) = prefs.edit().putString("last_mood_cursor", cursor).apply()

    fun getLastJournalCursor(): String? = prefs.getString("last_journal_cursor", null)
    fun saveLastJournalCursor(cursor: String) = prefs.edit().putString("last_journal_cursor", cursor).apply()

    fun clearCursors() = prefs.edit().clear().apply()
}