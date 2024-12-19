package ru.yarsu.storages

import ru.yarsu.models.Template
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
}
