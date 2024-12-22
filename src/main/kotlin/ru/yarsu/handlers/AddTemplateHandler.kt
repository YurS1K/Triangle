package ru.yarsu.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.lens.contentType
import ru.yarsu.models.Template
import ru.yarsu.storages.TemplateStorage
import java.util.UUID

class AddTemplateHandler(
    private val templateStorage: TemplateStorage,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val body = request.bodyString()
        val validateText = validate(body)

        if (validateText == "{}") {
            val json = body.asJsonObject()
            val template = templateStorage.getBySide(json["SideA"].asInt(), json["SideB"].asInt(), json["SideC"].asInt())

            if (template != null) {
                val mapper = jacksonObjectMapper()
                val node = mapper.createObjectNode()
                node.put("Id", template.id.toString())
                return Response(Status.CONFLICT)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(node))
            } else {
                val newID = UUID.randomUUID()

                val mapper = jacksonObjectMapper()
                val node = mapper.createObjectNode()
                node.put("Id", newID.toString())

                templateStorage.add(Template(newID, json["SideA"].asInt(), json["SideB"].asInt(), json["SideC"].asInt()))

                return Response(Status.CREATED)
                    .contentType(ContentType.APPLICATION_JSON)
                    .body(mapper.writeValueAsString(node))
            }
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
                    if (json.get("SideA").asInt() < 1) {
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
                    if (json.get("SideB").asInt() < 1) {
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
                    if (json.get("SideC").asInt() < 1) {
                        val sideCNode = mapper.createObjectNode()
                        sideCNode.putIfAbsent("Value", json.get("SideC"))
                        sideCNode.put("Error", "Ожидается натуральное значение")
                        errorNode.putIfAbsent("SideC", sideCNode)
                    }
                }
            }
            return mapper.writeValueAsString(errorNode)
        } catch (e: Exception) {
            errorNode.put("Value", body)
            errorNode.put("Error", "Missing a name for object member.")
            return mapper.writeValueAsString(errorNode)
        }
    }
}
