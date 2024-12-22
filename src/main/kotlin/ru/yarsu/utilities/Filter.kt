package ru.yarsu.utilities

import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.lens.contentType

val jsonContentTypeFilter =
    Filter { next: HttpHandler ->
        { request ->
            val response = next(request)
            if (response.bodyString().isNotEmpty()) {
                response.contentType(ContentType.APPLICATION_JSON)
            } else {
                response
            }
        }
    }
