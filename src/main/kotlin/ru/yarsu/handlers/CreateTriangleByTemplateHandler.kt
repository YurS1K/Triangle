package ru.yarsu.handlers

import org.http4k.core.Request
import org.http4k.core.Response
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.lens.contentType
import org.http4k.routing.path
import ru.yarsu.models.Color
import ru.yarsu.models.Triangle
import ru.yarsu.utilities.createError
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

class CreateTriangleByTemplateHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
    private val userStorage: UserStorage
) : HttpHandler{
    override fun invoke(request: Request): Response {
        val templateIDString = request.path("template-id").orEmpty()
        val body = request.bodyString()
        val validateText = validate(body)

        if (validateText == "{}")
        {
            val json = body.asJsonObject()
            try {
                if (templateIDString.isEmpty()) {
                    return Response(
                        Status.BAD_REQUEST,
                    ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
                }

                val template = templateStorage.getByID(UUID.fromString(templateIDString))
                    ?: return Response(Status.NOT_FOUND).contentType(ContentType.APPLICATION_JSON).body(createNotFoundError(templateIDString, "Шаблон не найден"))

                if(triangleStorage.getByTemplateID(template.id).isEmpty())
                {
                    templateStorage.delete(template)
                    return Response(Status.NO_CONTENT)
                }

                val newID = UUID.randomUUID()

                triangleStorage.add(Triangle(newID, template.id, if (json.has("RegistrationDateTime")) LocalDateTime.parse(json["RegistrationDateTime"].asText()) else LocalDateTime.parse(LocalDate.now().toString() + "T00:00:00"), Color.getType(json["BorderColor"].asText()), Color.getType(json["FillColor"].asText()), json["Description"].asText(), UUID.fromString(json["Owner"].asText())))

                val mapper = jacksonObjectMapper()
                val node = mapper.createObjectNode()
                node.put("Id", newID.toString())

                return Response(Status.CREATED).contentType(ContentType.APPLICATION_JSON).body(mapper.writeValueAsString(node))
            }
            catch (e :Exception)
            {
                return Response(
                    Status.BAD_REQUEST,
                ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
            }
        }
        else
        {
            return Response(Status.BAD_REQUEST).contentType(ContentType.APPLICATION_JSON).body(validateText)
        }
    }

    private fun validate(body: String) : String
    {
        val mapper = jacksonObjectMapper()
        val errorNode = mapper.createObjectNode()
        try {
            val json = body.asJsonObject()

            if (json.has("RegistrationDateTime")) {
                try {
                    LocalDateTime.parse(json.get("RegistrationDateTime").asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: DateTimeParseException) {
                    val dateNode = mapper.createObjectNode()
                    dateNode.putIfAbsent("Value", json.get("RegistrationDateTime"))
                    dateNode.put("Error", "Ожидается корректные дата и время")
                    errorNode.putIfAbsent("RegistrationDateTime", dateNode)
                }
            }

            if (!json.has("BorderColor")) {
                val colorNode = mapper.createObjectNode()
                colorNode.putIfAbsent("Value", json.get("BorderColor"))
                colorNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("BorderColor", colorNode)
            } else {
                try {
                    Color.getType(json.get("BorderColor").asText())
                } catch (e: IllegalArgumentException) {
                    val colorNode = mapper.createObjectNode()
                    colorNode.putIfAbsent("Value", json.get("BorderColor"))
                    colorNode.put("Error", "Ожидается цвет из списка")
                    errorNode.putIfAbsent("BorderColor", colorNode)
                }
            }

            if (!json.has("FillColor")) {
                val colorNode = mapper.createObjectNode()
                colorNode.putIfAbsent("Value", json.get("FillColor"))
                colorNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("FillColor", colorNode)
            } else {
                try {
                    Color.getType(json.get("FillColor").asText())
                } catch (e: IllegalArgumentException) {
                    val colorNode = mapper.createObjectNode()
                    colorNode.putIfAbsent("Value", json.get("FillColor"))
                    colorNode.put("Error", "Ожидается цвет из списка")
                    errorNode.putIfAbsent("FillColor", colorNode)
                }
            }

            if (!json.has("Description")) {
                val descriptionNode = mapper.createObjectNode()
                descriptionNode.putIfAbsent("Value", json.get("Description"))
                descriptionNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("Description", descriptionNode)
            } else {
                if (!json.get("Description").isTextual) {
                    val descriptionNode = mapper.createObjectNode()
                    descriptionNode.putIfAbsent("Value", json.get("Description"))
                    descriptionNode.put("Error", "Ожидается строка")
                    errorNode.putIfAbsent("Description", descriptionNode)
                }
            }

            if (json.has("Owner")) {
                try {
                    UUID.fromString(json.get("Owner").asText())
                    if (userStorage.getByID(UUID.fromString(json.get("Owner").asText())) == null) {
                        val ownerNode = mapper.createObjectNode()
                        ownerNode.putIfAbsent("Value", json.get("Owner"))
                        ownerNode.put("Error", "Работник с UUID ${json.get("Owner")} не найден")
                        errorNode.putIfAbsent("Owner", ownerNode)
                    }
                } catch (e: IllegalArgumentException) {
                    val ownerNode = mapper.createObjectNode()
                    ownerNode.putIfAbsent("Value", json.get("Owner"))
                    ownerNode.put("Error", "Ожидается корректный UUID, но получено ${json.get("Owner")}")
                    errorNode.putIfAbsent("Owner", ownerNode)
                }
            } else {
                val ownerNode = mapper.createObjectNode()
                ownerNode.putNull("Value")
                ownerNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("Owner", ownerNode)
            }

        }
        catch (e: Exception)
        {
            errorNode.put("Value", body)
            errorNode.put("Error", "Missing a name for object member.")
            return mapper.writeValueAsString(errorNode)
        }
        return mapper.writeValueAsString(errorNode)
    }

    private fun createNotFoundError(
        templateID: String,
        message: String,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
        val node = mapper.createObjectNode()
        node.put("TemplateId", templateID)
        node.put("Error", message)
        return mapper.writeValueAsString(node)
    }
}
