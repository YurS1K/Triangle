package ru.yarsu.handlers

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType
import org.http4k.routing.path
import ru.yarsu.models.Template
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.utilities.createError
import java.util.UUID

class GetInfoTemplateHandler(
    private val templateStorage: TemplateStorage,
): HttpHandler {
    override fun invoke(request: Request): Response {
        val templateIDString = request.path("template-id").orEmpty()
        try {
            if (templateIDString.isEmpty()) {
                return Response(
                    Status.BAD_REQUEST,
                ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
            }

            val template = templateStorage.getByID(UUID.fromString(templateIDString))
                ?: return Response(Status.NOT_FOUND).contentType(ContentType.APPLICATION_JSON).body(createNotFoundError(templateIDString, "Шаблон не найден"))

            return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(createObject(template))
        }
        catch (e :Exception)
        {
            return Response(
                Status.BAD_REQUEST,
            ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
        }
    }


    private fun createNotFoundError(
        templateID: String,
        message: String,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()
        node.put("TemplateId", templateID)
        node.put("Error", message)
        return mapper.writeValueAsString(node)
    }

    private fun createObject(
        template: Template,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())

        val node = mapper.createObjectNode()
        node.put("Id", template.id.toString())
        node.put("SideA", template.sideA)
        node.put("SideB", template.sideB)
        node.put("SideC", template.sideC)
        node.put("Area", template.area)
        node.put("Type", template.type.type)

        return mapper.writeValueAsString(node)
    }
}
