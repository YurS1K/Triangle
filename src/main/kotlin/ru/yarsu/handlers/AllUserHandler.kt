package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType

import ru.yarsu.storages.UserStorage

class AllUserHandler(
    private val userStorage: UserStorage
) : HttpHandler
{
    override fun invoke(request: Request): Response {
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()

        for (user in userStorage.getList())
        {
            val node = mapper.createObjectNode()
            node.put("Id", user.id.toString())
            node.put("Login", user.login)
            node.put("RegistrationDateTime", user.registrationDateTime.toString())
            node.put("Email", user.email)
            arrayNode.add(node)
        }

        return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(mapper.writeValueAsString(arrayNode))
    }
}
