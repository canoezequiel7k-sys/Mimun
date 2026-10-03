package com.canoezequiel.moodflow.data.local.sync

import com.canoezequiel.moodflow.data.local.dao.JournalEntryDao
import com.canoezequiel.moodflow.data.local.dao.MoodEntryDao
import com.canoezequiel.moodflow.data.local.entity.JournalEntryEntity
import com.canoezequiel.moodflow.data.local.entity.MoodEntryEntity
import com.canoezequiel.moodflow.data.local.remote.api.ApiService
import com.canoezequiel.moodflow.data.local.remote.dto.*
import com.canoezequiel.moodflow.data.remote.sync.SyncManager
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SyncManagerTest {

    private class FakeMoodEntryDao : MoodEntryDao {
        val storage = mutableMapOf<String, MoodEntryEntity>()

        override fun getAllMoodEntries(): List<MoodEntryEntity> = storage.values.toList()
        override fun getMoodEntryByDate(dateString: String): MoodEntryEntity? = storage.values.find { it.timestamp.startsWith(dateString) && it.deletedAt == null }
        override fun getMoodEntryById(id: String): MoodEntryEntity? = storage[id]
        override fun insertMoodEntry(entry: MoodEntryEntity) { storage[entry.id] = entry }
        override fun softDeleteMoodEntriesByDate(dateString: String, deletedAt: String) {}
        override fun softDeleteMoodEntryById(id: String, deletedAt: String) {}
        override fun deleteMoodEntryPermanently(id: String) { storage.remove(id) }
        override fun getPendingMoodEntries(): List<MoodEntryEntity> = storage.values.filter { it.syncStatus != "SYNCED" }
        override fun markAsSynced(id: String) {
            storage[id]?.let { storage[id] = it.copy(syncStatus = "SYNCED") }
        }
        override fun deleteAllMoodEntries() { storage.clear() }
    }

    private class FakeJournalEntryDao : JournalEntryDao {
        val storage = mutableMapOf<String, JournalEntryEntity>()

        override fun getAllJournalEntries(): List<JournalEntryEntity> = storage.values.toList()
        override fun getJournalEntryById(id: String): JournalEntryEntity? = storage[id]
        override fun insertJournalEntry(entry: JournalEntryEntity) { storage[entry.id] = entry }
        override fun softDeleteJournalEntryById(id: String, deletedAt: String) {}
        override fun deleteJournalEntryPermanently(id: String) { storage.remove(id) }
        override fun getPendingJournalEntries(): List<JournalEntryEntity> = storage.values.filter { it.syncStatus != "SYNCED" }
        override fun markAsSynced(id: String) {
            storage[id]?.let { storage[id] = it.copy(syncStatus = "SYNCED") }
        }
        override fun deleteAllJournalEntries() { storage.clear() }
    }

    private class FakeApiService : ApiService {
        var moodListResponse = PageDto<MoodEntryDto>(emptyList(), 0, 100, 0)
        var upsertMoodResponse: Response<MoodEntryDto> = Response.success(MoodEntryDto(id = "1", mood = "GOOD", note = null, date = "2026-10-03", editedAt = null, createdAt = null, updatedAt = null, deletedAt = null))
        var deleteMoodResponse: Response<Unit> = Response.success(Unit)

        var journalListResponse = PageDto<JournalEntryDto>(emptyList(), 0, 100, 0)
        var upsertJournalResponse: Response<JournalEntryDto> = Response.success(JournalEntryDto(id = "1", title = "Title", content = "Content", moodEntryId = null, editedAt = null, createdAt = null, updatedAt = null, deletedAt = null))
        var deleteJournalResponse: Response<Unit> = Response.success(Unit)

        override suspend fun getMoodEntries(limit: Int, offset: Int, updatedSince: String?): PageDto<MoodEntryDto> = moodListResponse
        override suspend fun upsertMoodEntry(id: String, entry: MoodEntryUpsertRequest): Response<MoodEntryDto> = upsertMoodResponse
        override suspend fun deleteMoodEntry(id: String): Response<Unit> = deleteMoodResponse

        override suspend fun getJournalEntries(limit: Int, offset: Int, updatedSince: String?): PageDto<JournalEntryDto> = journalListResponse
        override suspend fun upsertJournalEntry(id: String, entry: JournalEntryUpsertRequest): Response<JournalEntryDto> = upsertJournalResponse
        override suspend fun deleteJournalEntry(id: String): Response<Unit> = deleteJournalResponse

        override suspend fun register(request: RegisterRequest): AuthResponse = TODO()
        override suspend fun login(request: LoginRequest): AuthResponse = TODO()
        override suspend fun refreshToken(request: RefreshRequest): AuthResponse = TODO()
        override suspend fun logout(request: RefreshRequest): Response<Unit> = Response.success(Unit)
    }

    @Test
    fun pushMoodEntry_successMarksSynced() = runBlocking {
        val moodDao = FakeMoodEntryDao()
        val journalDao = FakeJournalEntryDao()
        val api = FakeApiService()
        val prefs = SyncPreferences(null)
        val syncManager = SyncManager(api, moodDao, journalDao, prefs)

        val entity = MoodEntryEntity(
            id = "test-id-1",
            moodType = "GOOD",
            note = "Hello",
            timestamp = "2026-10-03T10:00:00",
            syncStatus = "PENDING",
            updatedAt = "2026-10-03T10:00:00Z"
        )
        moodDao.insertMoodEntry(entity)

        api.upsertMoodResponse = Response.success(MoodEntryDto(id = "test-id-1", mood = "GOOD", note = "Hello", date = "2026-10-03", editedAt = null, createdAt = null, updatedAt = "2026-10-03T10:00:00Z", deletedAt = null))

        val result = syncManager.syncAll()
        assertTrue(result.isSuccess)
        assertEquals("SYNCED", moodDao.getMoodEntryById("test-id-1")?.syncStatus)
    }

    @Test
    fun deleteMoodEntry_with404_removesPermanently() = runBlocking {
        val moodDao = FakeMoodEntryDao()
        val journalDao = FakeJournalEntryDao()
        val api = FakeApiService()
        val prefs = SyncPreferences(null)
        val syncManager = SyncManager(api, moodDao, journalDao, prefs)

        val entity = MoodEntryEntity(
            id = "test-id-del",
            moodType = "GOOD",
            note = "Delete me",
            timestamp = "2026-10-03T10:00:00",
            syncStatus = "PENDING_DELETE",
            updatedAt = "2026-10-03T10:00:00Z",
            deletedAt = "2026-10-03T10:00:00Z"
        )
        moodDao.insertMoodEntry(entity)
        api.deleteMoodResponse = Response.error(404, "Not Found".toResponseBody("text/plain".toMediaTypeOrNull()))

        val result = syncManager.syncAll()
        assertTrue(result.isSuccess)
        assertEquals(null, moodDao.getMoodEntryById("test-id-del"))
    }

    @Test
    fun pullMoodEntry_withTombstone_deletesLocally() = runBlocking {
        val moodDao = FakeMoodEntryDao()
        val journalDao = FakeJournalEntryDao()
        val api = FakeApiService()
        val prefs = SyncPreferences(null)
        val syncManager = SyncManager(api, moodDao, journalDao, prefs)

        // Local existing entry
        val entity = MoodEntryEntity(
            id = "test-id-pull",
            moodType = "BAD",
            note = "Old",
            timestamp = "2026-10-03T10:00:00",
            syncStatus = "SYNCED",
            updatedAt = "2026-10-03T10:00:00Z"
        )
        moodDao.insertMoodEntry(entity)

        // Server returns tombstone (deletedAt != null)
        val tombstoneDto = MoodEntryDto(
            id = "test-id-pull",
            mood = "BAD",
            note = null,
            date = "2026-10-03",
            editedAt = null,
            createdAt = null,
            updatedAt = "2026-10-03T12:00:00Z",
            deletedAt = "2026-10-03T12:00:00Z"
        )
        api.moodListResponse = PageDto(listOf(tombstoneDto), 1, 100, 0)

        val result = syncManager.syncAll()
        assertTrue(result.isSuccess)
        assertEquals(null, moodDao.getMoodEntryById("test-id-pull"))
    }

    @Test
    fun pushMoodEntry_conflict409_serverWins() = runBlocking {
        val moodDao = FakeMoodEntryDao()
        val journalDao = FakeJournalEntryDao()
        val api = FakeApiService()
        val prefs = SyncPreferences(null)
        val syncManager = SyncManager(api, moodDao, journalDao, prefs)

        val localEntity = MoodEntryEntity(
            id = "local-id",
            moodType = "MEH",
            note = "Local note",
            timestamp = "2026-10-03T10:00:00",
            syncStatus = "PENDING",
            updatedAt = "2026-10-03T10:00:00Z"
        )
        moodDao.insertMoodEntry(localEntity)

        // Simulate 409 Conflict
        api.upsertMoodResponse = Response.error(409, "Conflict".toResponseBody("text/plain".toMediaTypeOrNull()))

        // Server has newer editedAt
        val serverConflictDto = MoodEntryDto(
            id = "server-id",
            mood = "RAD",
            note = "Server note",
            date = "2026-10-03",
            editedAt = "2026-10-03T15:00:00Z",
            createdAt = null,
            updatedAt = "2026-10-03T15:00:00Z",
            deletedAt = null
        )
        api.moodListResponse = PageDto(listOf(serverConflictDto), 1, 100, 0)

        val result = syncManager.syncAll()
        assertTrue(result.isSuccess)
        // Local id should be removed, server entry inserted
        assertEquals(null, moodDao.getMoodEntryById("local-id"))
        val serverSaved = moodDao.getMoodEntryById("server-id")
        assertEquals("RAD", serverSaved?.moodType)
        assertEquals("SYNCED", serverSaved?.syncStatus)
    }
}
