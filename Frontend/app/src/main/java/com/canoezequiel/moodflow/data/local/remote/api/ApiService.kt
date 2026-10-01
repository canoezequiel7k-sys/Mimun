package com.canoezequiel.moodflow.data.local.remote.api

import com.canoezequiel.moodflow.data.local.remote.dto.JournalEntryDto
import retrofit2.http.Query
import com.canoezequiel.moodflow.data.local.remote.dto.MoodEntryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // --- MOOD ENTRIES ---
    @GET("mood-entries")
    suspend fun getMoodEntries(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): List<MoodEntryDto>

    @PUT("mood-entries/{id}")
    suspend fun upsertMoodEntry(
        @Path("id") id: String,
        @Body entry: MoodEntryDto
    ): Response<MoodEntryDto>

    @DELETE("mood-entries/{id}")
    suspend fun deleteMoodEntry(@Path("id") id: String): Response<Unit>


    // --- JOURNAL ENTRIES ---
    @GET("journal-entries")
    suspend fun getJournalEntries(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): List<JournalEntryDto>

    @PUT("journal-entries/{id}")
    suspend fun upsertJournalEntry(
        @Path("id") id: String,
        @Body entry: JournalEntryDto
    ): Response<JournalEntryDto>

    @DELETE("journal-entries/{id}")
    suspend fun deleteJournalEntry(@Path("id") id: String): Response<Unit>
}