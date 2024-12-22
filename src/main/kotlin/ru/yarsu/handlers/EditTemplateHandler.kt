package ru.yarsu.handlers

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import ru.yarsu.storages.TriangleStorage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.lens.contentType
import org.http4k.routing.path
import ru.yarsu.models.Template
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.utilities.createError
import java.util.UUID

class EditTemplateHandler(
    private val templateStorage: TemplateStorage,
) : HttpHandler{
    override fun invoke(request: Request): Response {
        val body = request.bodyString()
        val validateText = validate(body)

        if (validateText == "{}")
        {
            val json = body.asJsonObject()
            val templateIDString = request.path("template-id").orEmpty()
            try {
                if (templateIDString.isEmpty()) {
                    return Response(
                        Status.BAD_REQUEST,
                    ).body(createError("Некорректное значение переданного параметра id. Ожидается UUID, но получено текстовое значение"))
                }

                val template = templateStorage.getByID(UUID.fromString(templateIDString))
                    ?: return Response(Status.NOT_FOUND).contentType(ContentType.APPLICATION_JSON).body(createNotFoundError(templateIDString, "Шаблон не найден"))

                val templateBySide = templateStorage.getBySide(json["SideA"].asInt(), json["SideB"].asInt(), json["SideC"].asInt())
                if (templateBySide != null)
                {
                    val mapper = jacksonObjectMapper()
                    val node = mapper.createObjectNode()
                    node.put("Id", templateBySide.id.toString())
                    return Response(Status.CONFLICT).contentType(ContentType.APPLICATION_JSON).body(mapper.writeValueAsString(node))
                }

                templateStorage.delete(template)
                templateStorage.add(Template(template.id, json["SideA"].asInt(), json["SideB"].asInt(), json["SideC"].asInt()))
                return Response(Status.NO_CONTENT)
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

    private fun validate(body: String): String{
        val mapper = jacksonObjectMapper()
        val errorNode = mapper.createObjectNode()
        try {
            val json = body.asJsonObject()

            if (!json.has("SideA")) {
                val sideANode = mapper.createObjectNode()
                sideANode.putIfAbsent("Value", json.get("SideA"))
                sideANode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("SideA", sideANode)
            } else {
                if (!json.get("SideA").isIntegralNumber) {
                    val sideANode = mapper.createObjectNode()
                    sideANode.putIfAbsent("Value", json.get("SideA"))
                    sideANode.put("Error", "Ожидается натуральное значение")
                    errorNode.putIfAbsent("SideA", sideANode)
                } else {
                    if (json.get("SideA").asInt() >= 1) {
                        val sideANode = mapper.createObjectNode()
                        sideANode.putIfAbsent("Value", json.get("SideA"))
                        sideANode.put("Error", "Ожидается натуральное значение")
                        errorNode.putIfAbsent("SideA", sideANode)
                    }
                }
            }
            if (!json.has("SideB")) {
                val sideBNode = mapper.createObjectNode()
                sideBNode.putIfAbsent("Value", json.get("SideB"))
                sideBNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("SideB", sideBNode)
            } else {
                if (!json.get("SideB").isIntegralNumber) {
                    val sideBNode = mapper.createObjectNode()
                    sideBNode.putIfAbsent("Value", json.get("SideB"))
                    sideBNode.put("Error", "Ожидается натуральное значение")
                    errorNode.putIfAbsent("SideB", sideBNode)
                } else {
                    if (json.get("SideB").asInt() >= 1) {
                        val sideBNode = mapper.createObjectNode()
                        sideBNode.putIfAbsent("Value", json.get("SideB"))
                        sideBNode.put("Error", "Ожидается натуральное значение")
                        errorNode.putIfAbsent("SideB", sideBNode)
                    }
                }
            }
            if (!json.has("SideC")) {
                val sideCNode = mapper.createObjectNode()
                sideCNode.putIfAbsent("Value", json.get("SideC"))
                sideCNode.put("Error", "Поле отсутствует")
                errorNode.putIfAbsent("SideC", sideCNode)
            } else {
                if (!json.get("SideC").isIntegralNumber) {
                    val sideCNode = mapper.createObjectNode()
                    sideCNode.putIfAbsent("Value", json.get("SideC"))
                    sideCNode.put("Error", "Ожидается натуральное значение")
                    errorNode.putIfAbsent("SideC", sideCNode)
                } else {
                    if (json.get("SideC").asInt() >= 1) {
                        val sideCNode = mapper.createObjectNode()
                        sideCNode.putIfAbsent("Value", json.get("SideC"))
                        sideCNode.put("Error", "Ожидается натуральное значение")
                        errorNode.putIfAbsent("SideC", sideCNode)
                    }
                }
            }
            return mapper.writeValueAsString(errorNode)
        }
        catch (e: Exception)
        {
            errorNode.put("Value", body)
            errorNode.put("Error", "Missing a name for object member.")
            return mapper.writeValueAsString(errorNode)
        }
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

    private fun createObject(
        template: Template,
    ): String {
        val mapper = jacksonObjectMapper()
        mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())

        val node = mapper.createObjectNode()
        node.put("Id", template.id.toString())
        node.put("SideA", template.sideA)
        node.put("SideB", template.sideB)
        node.put("SideC", template.sideC)
        node.put("Area", template.area)
        node.put("Type", template.type.type)

        return mapper.writeValueAsString(node)
    }

}
