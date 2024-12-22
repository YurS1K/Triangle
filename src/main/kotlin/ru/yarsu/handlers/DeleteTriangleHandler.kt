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
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError
import java.lang.IllegalArgumentException
import java.util.UUID

class DeleteTriangleHandler(
    private val triangleStorage: TriangleStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val triangleIDString = request.path("triangle-id").orEmpty()
        if (triangleIDString.isEmpty()) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(
                    createError(
                        "Некорректный идентификатор треугольника. Для параметра triangle-id ожидается UUID, но получено значение $triangleIDString",
                    ),
                )
        }
        try {
            UUID.fromString(triangleIDString)
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
        }
        val triangle = triangleStorage.getByID(UUID.fromString(triangleIDString))

        if (triangle != null) {
            triangleStorage.delete(triangle)
            return Response(Status.NO_CONTENT)
        } else {
            return Response(Status.NOT_FOUND)
                .contentType(ContentType.APPLICATION_JSON)
                .body(createNotFoundError(triangleIDString, "Треугольник не найден"))
        }
    }

    private fun createNotFoundError(
        triangleID: String,
        message: String,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()
        node.put("TriangleId", triangleID)
        node.put("Error", message)
        return mapper.writeValueAsString(node)
    }
}
