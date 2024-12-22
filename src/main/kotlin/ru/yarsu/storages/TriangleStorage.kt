package ru.yarsu.storages

import ru.yarsu.models.Color
import ru.yarsu.models.Template
import ru.yarsu.models.Triangle
import ru.yarsu.models.TriangleType
import java.util.UUID
import kotlin.Comparator

class TriangleStorage(
    private val triangleList: MutableList<Triangle>,
) {
    fun add(triangle: Triangle) {
        triangleList.add(triangle)
    }

    fun filter(filterFunc:(Triangle)->Boolean): List<Triangle>{
        return triangleList.filter(filterFunc)
    }
    fun delete(triangle: Triangle)
    {
        triangleList.remove(triangle)
    }

    fun sortedWith(comparator: Comparator<in Triangle>): List<Triangle> = triangleList.sortedWith(comparator)

    fun getList(): List<Triangle> = triangleList

    fun getByID(id: UUID): Triangle? = triangleList.find { it.id == id }

    fun getByTemplateID(id: UUID): List<Triangle> = triangleList.filter {it.template == id}

    fun getByColor(color: Color): List<Triangle> = triangleList.filter { it.fillColor == color}

    fun getByType(type: TriangleType, templateStorage: TemplateStorage): List<Triangle> = triangleList.filter { templateStorage.getByID(it.template)?.type == type }

    fun deleteByOwner(id: UUID)
    {
        triangleList.removeIf { it.owner == id }
    }
}
