package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.findSingle
import org.http4k.core.queries
import org.http4k.lens.contentType
import ru.yarsu.models.Triangle
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError
import ru.yarsu.utilities.paginateList
import java.util.UUID

class GetListByAreaHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
): HttpHandler {
    override fun invoke(request: Request): Response {
        val queries = request.uri.queries()
        val minStr = queries.findSingle("area-min")
        val maxStr = queries.findSingle("area-max")

        try {
            minStr?.toDouble()
        }
        catch (e: Exception)
        {
            Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(createError("Некорректное значение нижней границы площади. Для параметра area-min ожидается число, но получено текстовое значение «$minStr»"))
        }

        try {
            minStr?.toDouble()
        }
        catch (e: Exception)
        {
            Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(createError("Некорректное значение нижней границы площади. Для параметра area-max ожидается число, но получено текстовое значение «$maxStr»"))
        }
        try {
            val areaMin = minStr?.toDouble()
            val areaMax = maxStr?.toDouble()

            if (areaMin == null && areaMax == null) {
                return Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON)
                    .body(createError("Отсутствуют параметры area-min и area-max"))
            }

            val templateList = templateStorage.getByArea(areaMin, areaMax)
            val idList = emptyList<UUID>().toMutableList()
            for (i in templateList) {
                idList.add(i.id)
            }

            val triangleList = emptyList<Triangle>().toMutableList()
            for(i in idList)
            {
                for(j in triangleStorage.getByTemplateID(i))
                {
                    triangleList.add(j)
                }
            }
            val paginated = paginateList(queries, triangleList.toList().sortedWith(compareBy(Triangle::registrationDateTime,Triangle::id)))

            return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(createObject(paginated))
        }
        catch (e: IllegalArgumentException) {
        return Response(Status.BAD_REQUEST)
            .contentType(ContentType.APPLICATION_JSON)
            .body(createError(e.message ?: ""))
        }

    }

    private fun createObject(triangleList: List<Triangle>): String{
        val mapper = jacksonObjectMapper()
        val arrayNode = mapper.createArrayNode()

        for (i in triangleList)
        {
            val node = mapper.createObjectNode()
            node.put("Id", i.id.toString())
            node.put("SideA", templateStorage.getByID(i.template)?.sideA)
            node.put("SideB", templateStorage.getByID(i.template)?.sideB)
            node.put("SideC", templateStorage.getByID(i.template)?.sideC)
            arrayNode.add(node)
        }

        return mapper.writeValueAsString(arrayNode)
    }
}
