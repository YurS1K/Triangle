package ru.yarsu.utilities

import org.http4k.core.Parameters
import org.http4k.core.findSingle
import ru.yarsu.models.Machine

private fun validatePagination(queryParams: Parameters) {
    val perPageValues = listOf(5, 10, 20, 50)
    val pageParsed = queryParams.findSingle("page")
    val recordsPerPageParsed = queryParams.findSingle("records-per-page")

    val page = (pageParsed ?: "1").toIntOrNull()
    val recordsPerPage = (recordsPerPageParsed ?: "10").toIntOrNull()

    if (page == null) {
        throw IllegalArgumentException(
            "Некорректное значение параметра page. Ожидается натуральное число, но получено $pageParsed",
        )
    }
    if (recordsPerPage == null) {
        throw IllegalArgumentException(
            "Некорректное значение параметра recordsPerPage. Ожидается натуральное число, но получено $recordsPerPageParsed",
        )
    }
    if (page <= 0) {
        throw IllegalArgumentException(
            "Ожидалось значение параметра page >= 1, но получено $page",
        )
    }
    if (recordsPerPage <= 0) {
        throw IllegalArgumentException(
            "Ожидалось значение параметра recordsPerPage >= 1, но получено $recordsPerPage",
        )
    }
    if (recordsPerPage !in perPageValues) {
        throw IllegalArgumentException(
            "Ожидалось одно из этих значений: $perPageValues, но получено $recordsPerPage",
        )
    }
}

fun paginateList(
    queryParams: Parameters,
    list: List<Machine>,
): List<Machine> {
    validatePagination(queryParams)
    val page = (queryParams.findSingle("page") ?: "1").toInt()
    val recordsPerPage = (queryParams.findSingle("records-per-page") ?: "10").toInt()
    val fin = page * recordsPerPage
    val start = fin - recordsPerPage

    if (start >= list.size) {
        return listOf()
    }

    if ((start - 1) < list.size && (fin - 1) >= list.size) {
        return list.subList(start, list.size)
    }

    return list.subList(start, fin)
}
