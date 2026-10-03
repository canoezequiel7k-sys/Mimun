package com.canoezequiel.moodflow.data.remote.sync

import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.data.local.sync.SyncPreferences
import com.canoezequiel.moodflow.data.mapper.toDomain
import com.canoezequiel.moodflow.data.mapper.toEntity
import com.canoezequiel.moodflow.data.mapper.toUpsertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncManager(
    private val api: ApiService,
    private val moodEntryDao: MoodEntryDao,
    private val journalEntryDao: JournalEntryDao,
    private val syncPreferences: SyncPreferences
) {

    suspend fun syncAll(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Push: Subir cambios locales pendientes de estados de ánimo
            pushMoodEntries()

            // 2. Push: Subir cambios locales pendientes de reflexiones
            pushJournalEntries()

            // 3. Pull: Descargar actualizaciones de estados de ánimo (primero los estados)
            pullMoodEntries()

            // 4. Pull: Descargar actualizaciones de reflexiones (después las reflexiones)
            pullJournalEntries()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pushMoodEntries() {
        val pendingMoods = moodEntryDao.getPendingMoodEntries()
        for (entity in pendingMoods) {
            if (entity.syncStatus == "PENDING_DELETE" || entity.deletedAt != null) {
                try {
                    val response = api.deleteMoodEntry(entity.id)
                    if (response.isSuccessful || response.code() == 404) {
                        moodEntryDao.deleteMoodEntryPermanently(entity.id)
                    }
                } catch (_: Exception) { }
            } else {
                try {
                    val domainModel = entity.toDomain()
                    val response = api.upsertMoodEntry(entity.id, domainModel.toUpsertRequest())
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            if (body.deletedAt != null) {
                                moodEntryDao.deleteMoodEntryPermanently(entity.id)
                            } else {
                                val serverEntity = body.toDomain().toEntity().copy(syncStatus = "SYNCED")
                                moodEntryDao.insertMoodEntry(serverEntity)
                            }
                        } else {
                            moodEntryDao.markAsSynced(entity.id)
                        }
                    } else if (response.code() == 409) {
                        // 409 MOOD_ENTRY_ALREADY_EXISTS: Conflicto de dos registros para el mismo día
                        handleMoodConflict(entity)
                    }
                } catch (_: Exception) { }
            }
        }
    }

    private suspend fun handleMoodConflict(localEntity: MoodEntryEntity) {
        try {
            val response = api.getMoodEntries(limit = 100, offset = 0)
            val serverConflict = response.items.find { it.date == localEntity.timestamp.take(10) }
            if (serverConflict != null) {
                val serverEditedAtStr = serverConflict.editedAt
                val localEditedAtStr = localEntity.updatedAt
                val serverInstant = try { serverEditedAtStr?.let { java.time.Instant.parse(it) } } catch (_: Exception) { null }
                val localInstant = try { localEditedAtStr?.let { java.time.Instant.parse(it) } } catch (_: Exception) { null }

                val isServerNewer = if (serverInstant != null && localInstant != null) {
                    serverInstant >= localInstant
                } else {
                    (serverEditedAtStr ?: "") >= (localEditedAtStr ?: "")
                }

                if (isServerNewer) {
                    // El servidor gana: adoptamos la versión del servidor
                    moodEntryDao.deleteMoodEntryPermanently(localEntity.id)
                    val syncedEntity = serverConflict.toDomain().toEntity().copy(syncStatus = "SYNCED")
                    moodEntryDao.insertMoodEntry(syncedEntity)
                } else {
                    // La versión local es más reciente: enviamos el upsert con el ID del servidor para sobrescribir
                    val localUpsert = localEntity.toDomain().toUpsertRequest()
                    val overwriteResp = api.upsertMoodEntry(serverConflict.id, localUpsert)
                    if (overwriteResp.isSuccessful) {
                        moodEntryDao.deleteMoodEntryPermanently(localEntity.id)
                        val body = overwriteResp.body()
                        if (body != null) {
                            moodEntryDao.insertMoodEntry(body.toDomain().toEntity().copy(syncStatus = "SYNCED"))
                        }
                    }
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun pushJournalEntries() {
        val pendingJournal = journalEntryDao.getPendingJournalEntries()
        for (entity in pendingJournal) {
            if (entity.syncStatus == "PENDING_DELETE" || entity.deletedAt != null) {
                try {
                    val response = api.deleteJournalEntry(entity.id)
                    if (response.isSuccessful || response.code() == 404) {
                        journalEntryDao.deleteJournalEntryPermanently(entity.id)
                    }
                } catch (_: Exception) { }
            } else {
                try {
                    val domainModel = entity.toDomain()
                    val response = api.upsertJournalEntry(entity.id, domainModel.toUpsertRequest())
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            if (body.deletedAt != null) {
                                journalEntryDao.deleteJournalEntryPermanently(entity.id)
                            } else {
                                val serverEntity = body.toDomain().toEntity().copy(syncStatus = "SYNCED")
                                journalEntryDao.insertJournalEntry(serverEntity)
                            }
                        } else {
                            journalEntryDao.markAsSynced(entity.id)
                        }
                    }
                } catch (_: Exception) { }
            }
        }
    }

    private suspend fun pullMoodEntries() {
        var cursor = syncPreferences.getLastMoodCursor()
        var offset = 0
        val limit = 100
        var maxCursorInBatch = cursor

        while (true) {
            val page = api.getMoodEntries(limit = limit, offset = offset, updatedSince = cursor)
            for (dto in page.items) {
                if (dto.deletedAt != null) {
                    moodEntryDao.deleteMoodEntryPermanently(dto.id)
                } else {
                    val entity = dto.toDomain().toEntity().copy(syncStatus = "SYNCED")
                    moodEntryDao.insertMoodEntry(entity)
                }
                dto.updatedAt?.let {
                    if (maxCursorInBatch == null || it > maxCursorInBatch!!) {
                        maxCursorInBatch = it
                    }
                }
            }
            offset += page.items.size
            if (page.items.size < limit) break
        }

        maxCursorInBatch?.let { syncPreferences.saveLastMoodCursor(it) }
    }

    private suspend fun pullJournalEntries() {
        var cursor = syncPreferences.getLastJournalCursor()
        var offset = 0
        val limit = 100
        var maxCursorInBatch = cursor

        while (true) {
            val page = api.getJournalEntries(limit = limit, offset = offset, updatedSince = cursor)
            for (dto in page.items) {
                if (dto.deletedAt != null) {
                    journalEntryDao.deleteJournalEntryPermanently(dto.id)
                } else {
                    val entity = dto.toDomain().toEntity().copy(syncStatus = "SYNCED")
                    journalEntryDao.insertJournalEntry(entity)
                }
                dto.updatedAt?.let {
                    if (maxCursorInBatch == null || it > maxCursorInBatch!!) {
                        maxCursorInBatch = it
                    }
                }
            }
            offset += page.items.size
            if (page.items.size < limit) break
        }

        maxCursorInBatch?.let { syncPreferences.saveLastJournalCursor(it) }
    }
}