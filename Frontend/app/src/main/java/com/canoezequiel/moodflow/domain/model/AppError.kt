package com.canoezequiel.moodflow.domain.model

sealed class AppError: Exception() {
    data class NotFound(override val message: String) : AppError()
    data class Conflict(override val message: String) : AppError()
    data class ValidationError(override val message: String) : AppError()
    data class TooManyRequests(override val message: String, val retryAfter: Int? = null) : AppError()
    data class ServerError(override val message: String) : AppError()
    data class NetworkError(override val message: String) : AppError()
    data class UnknownError(override val message: String) : AppError()
}