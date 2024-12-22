package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import ru.yarsu.models.Color
import ru.yarsu.models.Triangle
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError
import ru.yarsu.utilities.paginateList

class GetListByColorHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
) : HttpHandler {
    private fun createObject(triangles: List<Triangle>): String {
        val mapper = jacksonObjectMapper()
        val array = mapper.createArrayNode()

        for (triangle in triangles) {
            val node = mapper.createObjectNode()
            val template = templateStorage.getByID(triangle.template)
            node.put("Id", triangle.id.toString())
            if (template != null) {
                node.put("SideA", template.sideA)
                node.put("SideB", template.sideB)
                node.put("SideC", template.sideC)
                array.add(node)
            }
        }

        return mapper.writeValueAsString(array)
    }

    override fun invoke(request: Request): Response {
        try {
            val queryParams = request.uri.queries()
            val borderColorQuery =
                queryParams.findSingle("border-color")
                    ?: return Response(Status.BAD_REQUEST)
                        .body(createError("Некорректный цвет. Для параметра border-color ожидается цвет, но получено значение «color»"))
            val borderColor = Color.getType(borderColorQuery)

            val filteredList = triangleStorage.filter { it.borderColor == borderColor }
            val sortedList = filteredList.sortedWith(compareBy(Triangle::registrationDateTime, Triangle::id))
            val paginated = paginateList(queryParams, sortedList)

            val body = createObject(paginated)

            return Response(Status.OK)
                .body(body)
        } catch (e: NumberFormatException) {
            return Response(Status.BAD_REQUEST)
                .body(createError("Ожидалось натуральное число в параметре page"))
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .body(createError(e.message ?: ""))
        }
    }
}
