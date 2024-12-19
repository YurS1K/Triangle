package ru.yarsu.forCommander

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

@Parameters(separators = " ")
open class RequiredParameter {
    @Parameter(names = ["--machines-file"], required = true)
    var templatesFilePath: String? = null

    @Parameter(names = ["--launderettes-file"], required = true)
    var trianglesFilePath: String? = null

    @Parameter(names = ["--users-file"], required = true)
    var usersFilePath: String? = null

    @Parameter(names = ["--port"], required = true)
    var port: Int = 9000
}
