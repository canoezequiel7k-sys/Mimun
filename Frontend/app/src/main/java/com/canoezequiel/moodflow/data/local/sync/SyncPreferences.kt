package com.canoezequiel.moodflow.data.local.sync

import android.content.Context

open class SyncPreferences(context: Context?) {
    private val prefs = context?.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    private var memoryMoodCursor: String? = null
    private var memoryJournalCursor: String? = null

    open fun getLastMoodCursor(): String? = prefs?.getString("last_mood_cursor", null) ?: memoryMoodCursor
    open fun saveLastMoodCursor(cursor: String) {
        memoryMoodCursor = cursor
        prefs?.edit()?.putString("last_mood_cursor", cursor)?.apply()
    }

    open fun getLastJournalCursor(): String? = prefs?.getString("last_journal_cursor", null) ?: memoryJournalCursor
    open fun saveLastJournalCursor(cursor: String) {
        memoryJournalCursor = cursor
        prefs?.edit()?.putString("last_journal_cursor", cursor)?.apply()
    }

    open fun clearCursors() {
        memoryMoodCursor = null
        memoryJournalCursor = null
        prefs?.edit()?.clear()?.apply()
    }
}