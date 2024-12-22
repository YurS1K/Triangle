package ru.yarsu.storages

import ru.yarsu.models.Template
import ru.yarsu.models.TriangleType
import java.util.UUID

class TemplateStorage(
    private val templateList: MutableList<Template>,
) {
    fun add(template: Template) {
        templateList.add(template)
    }

    fun getList(): List<Template> = templateList

    fun getByID(id: UUID): Template? = templateList.find { it.id == id }

    fun delete(template: Template) {
        templateList.remove(template)
    }

    fun getByArea(min: Double?, max: Double?): List<Template>
    {
        val localMin = min ?: Double.NEGATIVE_INFINITY
        val localMax = max ?: Double.POSITIVE_INFINITY

        return templateList.filter { (it.area ?: Double.NaN) in localMin..localMax }
    }

    fun getBySide(a: Int, b: Int, c: Int): Template? = templateList.find {
        it.sideA == a && it.sideB == b && it.sideC == c
                || it.sideA == a && it.sideB == c && it.sideC == b
                || it.sideA == b && it.sideB == a && it.sideC == c
                || it.sideA == b && it.sideB == c && it.sideC == a
                || it.sideA == c && it.sideB == a && it.sideC == b
                || it.sideA == c && it.sideB == b && it.sideC == a
    }

}
