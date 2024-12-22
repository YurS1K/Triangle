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
import ru.yarsu.models.Color
import ru.yarsu.models.StatisticParams
import ru.yarsu.models.TriangleType
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.utilities.createError

class GetStatisticHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
) : HttpHandler
{
    override fun invoke(request: Request): Response {
        val queries = request.uri.queries()

        val byStr = queries.findSingle("by")
            ?: return Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(createError("Отсутствует параметр by"))

        try {
            StatisticParams.getType(byStr)
        }
        catch (e: IllegalArgumentException)
        {
            return Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(createError("Некорректное значение типа статистики. Для параметра by ожидается значение типа статистики, но получено «$byStr»"))
        }

        return Response(Status.OK).contentType(ContentType.APPLICATION_JSON).body(createStatistic(StatisticParams.getType(byStr)))
    }

    private fun createStatistic(by: StatisticParams): String
    {
        val mapper = jacksonObjectMapper()
        val node = mapper.createObjectNode()

        if(by == StatisticParams.Color || by == StatisticParams.ColorType) {
            val arrayNode = mapper.createArrayNode()
            for (i in Color.entries.sortedWith(compareBy(Color::color))) {
                if(triangleStorage.getByColor(i).isNotEmpty()) {
                    val colorNode = mapper.createObjectNode()
                    colorNode.put("color", i.color)
                    colorNode.put("count", triangleStorage.getByColor(i).size)
                    arrayNode.add(colorNode)
                }
            }
            node.putIfAbsent("statisticByColor", arrayNode)
        }

        if(by == StatisticParams.Type || by == StatisticParams.ColorType) {
            val arrayNode = mapper.createArrayNode()
            for (i in TriangleType.entries.sortedWith(compareBy(TriangleType::type))) {
                if (triangleStorage.getByType(i, templateStorage).isNotEmpty()) {
                    val typeNode = mapper.createObjectNode()
                    typeNode.put("type", i.type)
                    typeNode.put("count", triangleStorage.getByType(i, templateStorage).size)
                    arrayNode.add(typeNode)
                }
            }
            node.putIfAbsent("statisticByType", arrayNode)
        }

        return mapper.writeValueAsString(node)
    }
}
