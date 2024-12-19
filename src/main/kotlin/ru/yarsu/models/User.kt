package ru.yarsu.models

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val login: String,
    val registrationDateTime: LocalDateTime,
    val email: String,
)
