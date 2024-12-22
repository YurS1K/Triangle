package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.WebForm
import org.http4k.lens.contentType
import org.http4k.lens.string
import org.http4k.lens.webForm
import ru.yarsu.models.User
import ru.yarsu.storages.UserStorage
import java.time.LocalDateTime
import java.util.UUID

class AddUserHandler(
    private val users: UserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val loginField = FormField.string().required("Login")
        val emailField = FormField.string().required("Email")
        val formLens = Body.webForm(Validator.Feedback, loginField, emailField).toLens()
        val form = formLens(request)

        if (form.errors.isEmpty()) {
            val login = loginField(form)
            val email = emailField(form)

            val user = users.getByLogin(login)
            if (user != null) return Response(Status.CONFLICT)

            val newId = UUID.randomUUID()
            users.add(User(newId, login, LocalDateTime.now(), email))
            val mapper = jacksonObjectMapper()
            val node = mapper.createObjectNode()
            node.put("Id", newId.toString())

            return Response(Status.CREATED)
                .contentType(ContentType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(node))
        }
        return Response(Status.BAD_REQUEST)
            .contentType(ContentType.APPLICATION_JSON)
            .body(createErrorBody(form))
    }

    private fun createErrorBody(form: WebForm): String {
        val mapper = jacksonObjectMapper()
        val node = mapper.createObjectNode()

        if (form.errors.toString().contains("Login")) {
            val errorNode = mapper.createObjectNode()
            errorNode.put("Value", form.fields["Login"]?.get(0))
            errorNode.put("Error", "Отсутствует поле")
            node.putIfAbsent("Login", errorNode)
        }

        if (form.errors.toString().contains("Email")) {
            val errorNode = mapper.createObjectNode()
            errorNode.put("Value", form.fields["Email"]?.get(0))
            errorNode.put("Error", "Отсутствует поле")
            node.putIfAbsent("Email", errorNode)
        }

        return mapper.writeValueAsString(node)
    }
}
