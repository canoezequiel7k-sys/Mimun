package com.canoezequiel.moodflow.data.local.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class JournalEntryDto(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("mood_entry_id") val moodEntryId: String? = null,
    val icon: String? = null,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class JournalEntryUpsertRequest(
    @SerialName("mood_entry_id") val moodEntryId: String? = null,
    val title: String,
    val content: String,
    val icon: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("edited_at") val editedAt: String
)