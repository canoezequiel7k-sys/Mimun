package com.canoezequiel.moodflow.data.local.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//estructura JSON que expone el FastAPI del backend. Usamos @SerialName para mapear nombres en snake_case (como mood_type) a propiedades en camelCase de Kotlin
@Serializable
data class MoodEntryDto(
    val id: String,
    val mood: String, // RAD, GOOD, MEH, BAD, AWFUL
    val note: String? = null,
    val date: String, //Formato YYYY-MM-DD
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)
@Serializable
data class MoodEntryUpsertRequest(
    val mood: String,
    val date: String,
    val note: String? = null,
    @SerialName("edited_at") val editedAt: String
)