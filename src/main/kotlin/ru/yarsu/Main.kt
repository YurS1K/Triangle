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
import org.http4k.lens.contentType
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import ru.yarsu.forCommander.RequiredParameter
import ru.yarsu.models.Color
import ru.yarsu.models.Triangle
import ru.yarsu.models.Template
import ru.yarsu.models.User
import ru.yarsu.storages.TemplateStorage
import ru.yarsu.storages.TriangleStorage
import ru.yarsu.storages.UserStorage
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
    var firstStringWasSkip = false
    var triangleID: UUID?
    for (str in afterReader) {
        if (firstStringWasSkip) {
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
            firstStringWasSkip = true
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
    var firstStringWasSkip = false
    for (str in afterReader) {
        if (firstStringWasSkip) {
            template =
                Template(
                    UUID.fromString(str[0]),
                    str[1].toInt(),
                    str[2].toInt(),
                    str[3].toInt(),
                    
                )
            launderettesList.add(launderette)
        } else {
            firstStringWasSkip = true
            continue
        }
    }
    return launderettesList.toList()
}

fun createRoutes(
    templateStorage: TemplateStorage,
    triangleStorage: TriangleStorage,
    userStorage: UserStorage,
): RoutingHttpHandler {
    val allTrianglesHandler = AllTriangleHandler(templateStorage, triangleStorage, userStorage)
    val addTriangleHandler = AddTriangleHandler(templateStorage, triangleStorage, userStorage)
    val getTriangleInfoHandler = GetTriangleInfoHandler(templateStorage, triangleStorage, userStorage)
    val deleteTriangleHandler = DeleteTriangleHandler(templateStorage, triangleStorage, userStorage)
    val getListByColorHandler = GetListByColorHandler(templateStorage, triangleStorage, userStorage)
    val getListByAreaHandler = GetListByAreaHandler(templateStorage, triangleStorage,userStorage)
    val getStatisticHandler = GetStatisticHandler(templateStorage, triangleStorage, userStorage)
    val allTemplatesHandler = AllTemplateHandler(templateStorage, triangleStorage, userStorage)
    val addTemplateHandler = AddTemplateHandler(templateStorage, triangleStorage, userStorage)
    val getTemplateInfoHandler = GetInfoTemplateHandler(templateStorage, triangleStorage, userStorage)
    val deleteTemplateHandler = DeleteTemplateHandler(templateStorage, triangleStorage, userStorage)
    val editTemplateHandler = EditTemplateHandler(templateStorage, triangleStorage, userStorage)
    val createTriangleByTemplateHandler = CreateTriangleByTemplateHandler(templateStorage, triangleStorage, userStorage)
    val allUsersHandler = AllUserHandler(templateStorage, triangleStorage, userStorage)
    val addUserHandler = AddUserHanler(templateStorage, triangleStorage, userStorage)
    val deleteUserHandler = DeleteUserHangler(templateStorage, triangleStorage, userStorage)
    return routes(
        "triangles" bind Method.GET to allTrianglesHandler,
        "triangles" bind Method.POST to addTriangleHandler,
        "triangles/{triangle-id}" bind Method.GET to getTriangleInfoHandler,
        "triangles/{triangle-id}" bind Method.DELETE to deleteTriangleHandler,
        "triangles/by-border-color" bind Method.GET to getListByColorHandler,
        "triangles/by-area" bind Method.GET to getListByAreaHandler,
        "triangles/statistics" bind Method.GET to getStatisticHandler,
        "templates" bind Method.GET to allTemplatesHandler,
        "templates" bind Method.POST to addTemplateHandler,
        "templates/{template-id}" bind Method.GET to getTemplateInfoHandler,
        "templates/{template-id}" bind Method.DELETE to deleteTemplateHandler,
        "templates/{template-id}" bind Method.PUT to editTemplateHandler,
        "templates/{template-id}/triangle" bind Method.POST to createTriangleByTemplateHandler,
        "users" bind Method.GET to allUsersHandler,
        "users/{user-id}" bind Method.POST to addUserHandler,
        "users/{{user-id}}" bind Method.DELETE to deleteUserHandler,
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
                "Name",
                "RegistrationDateTime",
                "Email",
            ),
        )
        for (user in users.getList()) {
            writeRow(
                user.id,
                user.name,
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
                "Title",
                "Type",
                "StartDateTime",
                "Condition",
                "Capacity",
                "Price",
                "Duration",
                "Specialist",
                "Launderette",
            ),
        )
        for (triangle in triangles.getList()) {
            writeRow(
                machine.id,
                machine.title,
                machine.type.type,
                machine.startDateTime,
                machine.condition,
                machine.capacity,
                machine.price,
                machine.duration,
                machine.specialistId,
                machine.launderette,
            )
        }
    }
}

fun saveTemplates(
    filePath: String,
    templates: templateStorage,
) {
    csvWriter().open(filePath) {
        writeRow(
            listOf(
                "Id",
                "Address",
                "PhoneNumber",
                "WorkingHours",
            ),
        )
        for (launderette in launderettes.getList()) {
            writeRow(
                launderette.id,
                launderette.address,
                launderette.phoneNumber,
                launderette.workingHours,
            )
        }
    }
}

fun main(args: Array<String>) {
    try {
        val command = argumentsParser(args)
        val userList = readUsers(command.usersFilePath ?: "")
        val templateList = readFile(command.templatesFilePath ?: "")
        val triangleList = readLaunderettes(command.trianglesFilePath ?: "")

        val triangleStorage = TriangleStorage(triangleList.toMutableList())
        val userStorage = UserStorage(userList.toMutableList())
        val templateStorage = TemplateStorage(templateList.toMutableList())

        val apiRoutes = createRoutes(templateStorage, triangleStorage, userStorage)
        val app: HttpHandler =
            routes(
                "ping" bind Method.GET to {
                    Response(Status.OK).contentType(ContentType.TEXT_HTML).body("Приложение запущено")
                },
                "v2" bind apiRoutes,
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
