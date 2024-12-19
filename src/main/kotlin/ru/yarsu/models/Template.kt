package ru.yarsu.models

import java.util.UUID

data class Template (
    val id: UUID,
    val sideA : Int,
    val sideB : Int,
    val sideC : Int,
    val area : Number?,
    val type: TriangleType,
    )
