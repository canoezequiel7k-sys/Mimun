package com.canoezequiel.moodflow.data.local.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class JournalEntryDto(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("mood_entry_id") val moodEntryId: String? = null,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)