package ru.yarsu.models

import java.time.LocalDateTime
import java.util.UUID

data class Triangle(
    val id: UUID,
    val template: UUID,
    val registrationDateTime: LocalDateTime,
    val borderColor: Color,
    val fillColor: Color,
    val description: String,
    val owner: UUID,
)
