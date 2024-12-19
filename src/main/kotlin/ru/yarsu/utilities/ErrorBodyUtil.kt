package ru.yarsu.utilities

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun createError(message: String): String {
    val mapper = jacksonObjectMapper()
    mapper.setDefaultPrettyPrinter(DefaultPrettyPrinter())
    val node = mapper.createObjectNode()
    node.put("Error", message)
    return mapper.writeValueAsString(node)
}
