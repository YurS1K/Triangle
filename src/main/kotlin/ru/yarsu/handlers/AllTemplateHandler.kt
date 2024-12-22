package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import ru.yarsu.models.Template
import ru.yarsu.storages.TemplateStorage

class AllTemplateHandler(
    private val templateStorage: TemplateStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()

        for (i in templateStorage.getList().sortedWith(compareBy(Template::id))) {
            val node = mapper.createObjectNode()
            node.put("Id", i.id.toString())
            node.put("SideA", i.sideA)
            node.put("SideB", i.sideB)
            node.put("SideC", i.sideC)
            arrayNode.add(node)
        }

        return Response(Status.OK)
            .body(mapper.writeValueAsString(arrayNode))
    }
}
