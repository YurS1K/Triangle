package ru.yarsu.handlers

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.queries
import org.http4k.lens.contentType
import ru.yarsu.models.Triangle
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError
import ru.yarsu.utilities.paginateList
import java.time.format.DateTimeFormatter

class AllTriangleHandler(
    private val triangleStorage: TriangleStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        try {
            val queryParams = request.uri.queries()
            val sortedList = triangleStorage.sortedWith(compareBy(Triangle::registrationDateTime, Triangle::id))

            val paginated = paginateList(queryParams, sortedList)

            val body = createObject(paginated)

            return Response(Status.OK)
                .contentType(ContentType.APPLICATION_JSON)
                .body(body)
        } catch (e: NumberFormatException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(createError("Ожидалось натуральное число в параметре page"))
        } catch (e: IllegalArgumentException) {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(createError(e.message ?: ""))
        }
    }

    private fun createObject(triangles: List<Triangle>): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val array = mapper.createArrayNode()
        for (triangle in triangles) {
            val node = mapper.createObjectNode()
            node.put("Id", "${triangle.id}")
            node.put("RegistrationDateTime", triangle.registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            node.put("Description", triangle.description)
            array.add(node)
        }
        return mapper.writeValueAsString(array)
    }
}
