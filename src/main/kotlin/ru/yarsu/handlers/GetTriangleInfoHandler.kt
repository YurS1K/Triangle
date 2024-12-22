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
import ru.yarsu.models.Triangle
import ru.yarsu.models.User
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import ru.yarsu.utilities.createError
import java.time.format.DateTimeFormatter
import java.util.UUID

class GetTriangleInfoHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
    private val userStorage: UserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val triangleIDString = request.path("triangle-id").orEmpty()
        try {
            if (triangleIDString.isEmpty()) {
                return Response(
                    Status.BAD_REQUEST,
                ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
            }

            val triangleID = UUID.fromString(triangleIDString)
            val triangle =
                triangleStorage.getByID(triangleID)
                    ?: return Response(Status.NOT_FOUND)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(createNotFoundError(triangleIDString, "Треугольник не найден"))

            val owner =
                userStorage.getByID(triangle.owner)
                    ?: return Response(Status.NOT_FOUND)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(
                            createNotFoundError(
                                triangleIDString,
                                "Владелец с id ${triangle.owner} не найден",
                            ),
                        )

            val template = templateStorage.getByID(triangle.template)

            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(createObject(triangle, owner, template))
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(
                    createError(
                        "Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение",
                    ),
                )
        }
    }

    private fun createNotFoundError(
        machineID: String,
        message: String,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()
        node.put("MachineId", machineID)
        node.put("Error", message)
        return mapper.writeValueAsString(node)
    }

    private fun createObject(
        triangle: Triangle,
        owner: User,
        template: Template?,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())

        val node = mapper.createObjectNode()
        node.put("Id", triangle.id.toString())
        node.put("Template", triangle.template.toString())
        if (template != null) {
            node.put("SideA", template.sideA)
            node.put("SideB", template.sideB)
            node.put("SideC", template.sideC)
            if (template.area != null) {
                node.put("Area", template.area)
            } else {
                node.putIfAbsent("Area", null)
            }
            node.put("Type", template.type.type)
        }
        node.put("RegistrationDateTime", triangle.registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        node.put("BorderColor", triangle.borderColor.color)
        node.put("FillColor", triangle.fillColor.color)
        node.put("Description", triangle.description)
        node.put("Owner", owner.id.toString())
        node.put("OwnerLogin", owner.login)

        return mapper.writeValueAsString(node)
    }
}
