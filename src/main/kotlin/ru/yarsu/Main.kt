package ru.yarsu

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.lens.contentType
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.forCommander.RequiredParameter
import ru.yarsu.handlers.AddTemplateHandler
import ru.yarsu.handlers.AddTriangleHandler
import ru.yarsu.handlers.AddUserHandler
import ru.yarsu.handlers.AllTemplateHandler
import ru.yarsu.handlers.AllTriangleHandler
import ru.yarsu.handlers.AllUserHandler
import ru.yarsu.handlers.CreateTriangleByTemplateHandler
import ru.yarsu.handlers.DeleteTemplateHandler
import ru.yarsu.handlers.DeleteTriangleHandler
import ru.yarsu.handlers.DeleteUserHandler
import ru.yarsu.handlers.EditTemplateHandler
import ru.yarsu.handlers.GetInfoTemplateHandler
import ru.yarsu.handlers.GetListByAreaHandler
import ru.yarsu.handlers.GetListByColorHandler
import ru.yarsu.handlers.GetStatisticHandler
import ru.yarsu.handlers.GetTriangleInfoHandler
import ru.yarsu.models.Color
import ru.yarsu.models.Template
import ru.yarsu.models.Triangle
import ru.yarsu.models.User
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
import ru.yarsu.utilities.jsonContentTypeFilter
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun argumentsParser(args: Array<String>): RequiredParameter {
    val reqParam = RequiredParameter()

    val commander: JCommander =
        JCommander
            .newBuilder()
            .addObject(reqParam)
            .build()
    commander.parse(*args)
    return reqParam
}

fun readFile(filePath: String): List<Triangle> {
    val result: MutableList<Triangle> = mutableListOf()
    var triangle: Triangle?
    if (!File(filePath).exists()) {
        throw FileNotFoundException("Error: file not found «$filePath»")
    }

    val afterReader = csvReader().readAll(File(filePath))
    var firstStringWasSkipped = false
    var triangleID: UUID?
    for (str in afterReader) {
        if (firstStringWasSkipped) {
            triangle =
                Triangle(
                    UUID.fromString(str[0]),
                    UUID.fromString(str[1]),
                    LocalDateTime.parse(str[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    Color.getType(str[3]),
                    Color.getType(str[4]),
                    str[5],
                    UUID.fromString(str[6]),
                )
            result.add(triangle)
        } else {
            firstStringWasSkipped = true
            continue
        }
    }
    return result.toList()
}

fun readUsers(filePath: String): List<User> {
    val usersList: MutableList<User> = mutableListOf()
    var user: User?
    if (!File(filePath).exists()) {
        throw FileNotFoundException("Error: file not found «$filePath»")
    }

    val afterReader = csvReader().readAll(File(filePath))
    var firstStringWasSkip = false
    for (str in afterReader) {
        if (firstStringWasSkip) {
            user =
                User(
                    UUID.fromString(str[0]),
                    str[1],
                    LocalDateTime.parse(str[2], DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    str[3],
                )
            usersList.add(user)
        } else {
            firstStringWasSkip = true
            continue
        }
    }
    return usersList.toList()
}

fun readTemplates(filePath: String): List<Template> {
    val templateList: MutableList<Template> = mutableListOf()
    var template: Template?
    if (!File(filePath).exists()) {
        throw FileNotFoundException("Error: file not found «$filePath»")
    }

    val afterReader = csvReader().readAll(File(filePath))
    var firstStringWasSkipped = false
    for (str in afterReader) {
        if (firstStringWasSkipped) {
            template =
                Template(
                    UUID.fromString(str[0]),
                    str[1].toInt(),
                    str[2].toInt(),
                    str[3].toInt(),
                )
            templateList.add(template)
        } else {
            firstStringWasSkipped = true
            continue
        }
    }
    return templateList.toList()
}

fun createRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
): RoutingHttpHandler {
    val allTrianglesHandler = AllTriangleHandler(triangleStorage)
    val addTriangleHandler = AddTriangleHandler(templateStorage, triangleStorage, userStorage)
    val getTriangleInfoHandler = GetTriangleInfoHandler(templateStorage, triangleStorage, userStorage)
    val deleteTriangleHandler = DeleteTriangleHandler(triangleStorage)
    val getListByColorHandler = GetListByColorHandler(templateStorage, triangleStorage)
    val getListByAreaHandler = GetListByAreaHandler(templateStorage, triangleStorage)
    val getStatisticHandler = GetStatisticHandler(templateStorage, triangleStorage)
    val allTemplatesHandler = AllTemplateHandler(templateStorage)
    val addTemplateHandler = AddTemplateHandler(templateStorage)
    val getTemplateInfoHandler = GetInfoTemplateHandler(templateStorage)
    val deleteTemplateHandler = DeleteTemplateHandler(templateStorage, triangleStorage)
    val editTemplateHandler = EditTemplateHandler(templateStorage)
    val createTriangleByTemplateHandler = CreateTriangleByTemplateHandler(templateStorage, triangleStorage, userStorage)
    val allUsersHandler = AllUserHandler(userStorage)
    val addUserHandler = AddUserHandler(userStorage)
    val deleteUserHandler = DeleteUserHandler(triangleStorage, userStorage)

    return routes(
        "triangles" bind Method.GET to allTrianglesHandler,
        "triangles" bind Method.POST to addTriangleHandler,
        "triangles/by-border-color" bind Method.GET to getListByColorHandler,
        "triangles/by-area" bind Method.GET to getListByAreaHandler,
        "triangles/statistics" bind Method.GET to getStatisticHandler,
        "triangles/{triangle-id}" bind Method.GET to getTriangleInfoHandler,
        "triangles/{triangle-id}" bind Method.DELETE to deleteTriangleHandler,
        "templates" bind Method.GET to allTemplatesHandler,
        "templates" bind Method.POST to addTemplateHandler,
        "templates/{template-id}" bind Method.GET to getTemplateInfoHandler,
        "templates/{template-id}" bind Method.DELETE to deleteTemplateHandler,
        "templates/{template-id}" bind Method.PUT to editTemplateHandler,
        "templates/{template-id}/triangle" bind Method.POST to createTriangleByTemplateHandler,
        "users" bind Method.GET to allUsersHandler,
        "users/" bind Method.POST to addUserHandler,
        "users/{user-id}" bind Method.DELETE to deleteUserHandler,
    )
}

fun saveUsers(
    filePath: String,
    users: UserStorage,
) {
    csvWriter().open(filePath) {
        writeRow(
            listOf(
                "Id",
                "Login",
                "RegistrationDateTime",
                "Email",
            ),
        )
        for (user in users.getList()) {
            writeRow(
                user.id,
                user.login,
                user.registrationDateTime,
                user.email,
            )
        }
    }
}

fun saveTriangles(
    filePath: String,
    triangles: TriangleStorage,
) {
    csvWriter().open(filePath) {
        writeRow(
            listOf(
                "Id",
                "Template",
                "RegistrationDateTime",
                "BorderColor",
                "FillColor",
                "Description",
                "Owner",
            ),
        )
        for (triangle in triangles.getList()) {
            writeRow(
                triangle.id.toString(),
                triangle.template.toString(),
                triangle.registrationDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                triangle.borderColor.color,
                triangle.fillColor.color,
                triangle.description,
                triangle.owner.toString(),
            )
        }
    }
}

fun saveTemplates(
    filePath: String,
    templates: TemplateStorage,
) {
    csvWriter().open(filePath) {
        writeRow(
            listOf(
                "Id",
                "SideA",
                "SideB",
                "SideC",
                "Type",
            ),
        )
        for (template in templates.getList()) {
            writeRow(
                template.id.toString(),
                template.sideA,
                template.sideB,
                template.sideC,
                template.type.type,
            )
        }
    }
}

fun main(args: Array<String>) {
    try {
        val command = argumentsParser(args)
        val userList = readUsers(command.usersFilePath ?: "")
        val triangleList = readFile(command.trianglesFilePath ?: "")
        val templateList = readTemplates(command.templatesFilePath ?: "")

        val triangleStorage = TriangleStorage(triangleList.toMutableList())
        val userStorage = UserStorage(userList.toMutableList())
        val templateStorage = TemplateStorage(templateList.toMutableList())

        val apiRoutes = createRoutes(templateStorage, triangleStorage, userStorage)

        val app: HttpHandler =
            jsonContentTypeFilter.then(
                routes(
                    "ping" bind Method.GET to {
                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body("Приложение запущено")
                    },
                    "v2" bind apiRoutes,
                ),
            )
        app.asServer(Netty(command.port)).start()
        Runtime
            .getRuntime()
            .addShutdownHook(
                thread(
                    false,
                    block = {
                        saveTriangles(command.trianglesFilePath ?: "", triangleStorage)
                        saveTemplates(command.templatesFilePath ?: "", templateStorage)
                        saveUsers(command.usersFilePath ?: "", userStorage)
                    },
                ),
            )
    } catch (e: ParameterException) {
        println(e.message)
        exitProcess(1)
    } catch (e: FileNotFoundException) {
        println(e.message)
        exitProcess(1)
    }
}
