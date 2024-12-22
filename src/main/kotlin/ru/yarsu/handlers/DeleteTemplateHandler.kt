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
import ru.yarsu.models.Triangle
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError
import java.util.UUID

class DeleteTemplateHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val templateIDString = request.path("template-id").orEmpty()
        try {
            if (templateIDString.isEmpty()) {
                return Response(
                    Status.BAD_REQUEST,
                ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
            }

            val template =
                templateStorage.getByID(UUID.fromString(templateIDString))
                    ?: return Response(
                        Status.NOT_FOUND,
                    ).contentType(ContentType.APPLICATION_JSON).body(createNotFoundError(templateIDString, "Шаблон не найден"))

            if (triangleStorage.getByTemplateID(template.id).isEmpty()) {
                templateStorage.delete(template)
                return Response(Status.NO_CONTENT)
            }

            return Response(
                Status.CONFLICT,
            ).contentType(ContentType.APPLICATION_JSON).body(createObject(triangleStorage.getByTemplateID(template.id)))
        } catch (e: Exception) {
            return Response(
                Status.BAD_REQUEST,
            ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
        }
    }

    private fun createObject(triangleList: List<Triangle>): String {
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()

        for (i in triangleList) {
            val node = mapper.createObjectNode()
            node.put("Id", i.id.toString())
            node.put("RegistrationDateTime", i.registrationDateTime.toString())
            node.put("Description", i.description)
            arrayNode.add(node)
        }

        return mapper.writeValueAsString(arrayNode)
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
}
