package ru.yarsu.storages

import ru.yarsu.models.Triangle
import java.util.UUID
import kotlin.Comparator

class TriangleStorage(
    private val triangleList: MutableList<Triangle>,
) {
    fun add(triangle: Triangle) {
        triangleList.add(triangle)
    }

    fun sortedWith(comparator: Comparator<in Triangle>): List<Triangle> = triangleList.sortedWith(comparator)

    fun getList(): List<Triangle> = triangleList

    fun getByID(id: UUID): Triangle? = triangleList.find { it.id == id }
}
