package ru.yarsu.handlers

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import ru.yarsu.utilities.createError
import java.util.UUID

class DeleteUserHandler(
    private val triangleStorage: TriangleStorage,
    private val userStorage: UserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val userIDString = request.path("user-id").orEmpty()
        try {
            if (userIDString.isEmpty()) {
                return Response(Status.BAD_REQUEST)
                    .body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
            }

            val user =
                userStorage.getByID(UUID.fromString(userIDString))
                    ?: return Response(Status.NOT_FOUND)
                        .body(createNotFoundError(userIDString, "Пользователь не найден"))

            triangleStorage.deleteByOwner(user.id)
            userStorage.delete(user)

            return Response(Status.NO_CONTENT)
        } catch (e: Exception) {
            return Response(Status.BAD_REQUEST)
                .body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
        }
    }

    private fun createNotFoundError(
        userID: String,
        message: String,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()
        node.put("UserId", userID)
        node.put("Error", message)
        return mapper.writeValueAsString(node)
    }
}
