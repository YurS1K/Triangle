package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.lens.contentType
import ru.yarsu.models.Color
import ru.yarsu.models.Triangle
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import java.lang.IllegalArgumentException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID

class AddTriangleHandler(
    private val templateStorage: TemplateStorage,
    private val triangleStorage: TriangleStorage,
    private val userStorage: UserStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val body = request.bodyString()
        val validateText = validate(body)
        if (validateText == "{}") {
            val json = body.asJsonObject()
            val newID = UUID.randomUUID()
            triangleStorage.add(
                Triangle(
                    newID,
                    UUID.fromString(json["Template"].asText()),
                    if (json.has("RegistrationDateTime")) {
                        LocalDateTime.parse(json["RegistrationDateTime"].asText())
                    } else {
                        LocalDateTime.now()
                    },
                    Color.getType(json["BorderColor"].asText()),
                    Color.getType(json["FillColor"].asText()),
                    json["Description"].asText(),
                    UUID.fromString(json["Owner"].asText()),
                ),
            )
            val mapper = jacksonObjectMapper()
            val node = mapper.createObjectNode()
            node.put("Id", newID.toString())
            return Response(Status.CREATED)
                .contentType(ContentType.APPLICATION_JSON)
                .body(mapper.writeValueAsString(node))
        } else {
            return Response(Status.BAD_REQUEST)
                .contentType(ContentType.APPLICATION_JSON)
                .body(validateText)
        }
    }

    private fun validate(body: String): String {
        val mapper = jacksonObjectMapper()
        val errorNode = mapper.createObjectNode()
        try {
            val json = body.asJsonObject()

            if (json.has("Template")) {
                try {
                    UUID.fromString(json.get("Template").asText())
                    if (templateStorage.getByID(UUID.fromString(json.get("Template").asText())) == null) {
                        val templateNode = mapper.createObjectNode()
                        templateNode.putIfAbsent("Value", json.get("Template"))
                        templateNode.put("Error", "Работник с UUID ${json.get("Template")} не найден")
                        errorNode.putIfAbsent("Template", templateNode)
                    }
                } catch (e: IllegalArgumentException) {
                    val templateNode = mapper.createObjectNode()
                    templateNode.putIfAbsent("Value", json.get("Template"))
                    templateNode.put("Error", "Ожидается корректный UUID, но получено ${json.get("Template")}")
                    errorNode.putIfAbsent("Template", templateNode)
                }
            } else {
                val templateNode = mapper.createObjectNode()
                templateNode.putNull("Value")
                templateNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("Template", templateNode)
            }

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
        } catch (e: Exception) {
            errorNode.put("Value", body)
            errorNode.put("Error", "Missing a name for object member.")
            return mapper.writeValueAsString(errorNode)
        }
        return mapper.writeValueAsString(errorNode)
    }
}
