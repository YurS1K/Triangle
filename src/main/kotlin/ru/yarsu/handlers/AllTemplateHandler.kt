package ru.yarsu.handlers

import ru.yarsu.storages.TemplateStorage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.contentType

class AllTemplateHandler(
    private val templateStorage: TemplateStorage,
) : HttpHandler {

    override fun invoke(request: Request): Response {
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()

        for(i in templateStorage.getList())
        {
            val node = mapper.createObjectNode()
            node.put("Id", i.id.toString())
            node.put("SideA", i.sideA)
            node.put("SideB", i.sideB)
            node.put("SideC", i.sideC)
            arrayNode.add(node)
        }

        return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(mapper.writeValueAsString(arrayNode))
    }
}
