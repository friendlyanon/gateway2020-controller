package gateway.controller

import gateway.controller.database.DbWrapper
import java.io.File
import kotlin.system.exitProcess
import org.mapdb.DBMaker

private fun usage(): Nothing {
    println(
        """
        Gateway controller for collecting, processing and storing sensor data.

        Usage:
          controller <port>
          controller -? | /? | -h | /h | --help | /help

        Options:
          -? /? -h /h --help /help  Print this message
    """.trimIndent()
    )
    exitProcess(0)
}

private fun notice() {
    val pkg = Master::class.java.`package`
    val title = pkg.specificationTitle
    val version = pkg.specificationVersion
    val vendor = pkg.specificationVendor
    println("$title - v$version (c) 2020 $vendor, All Rights Reserved")
}

private fun getDatabase() = DBMaker
    .fileDB(File("controller.db"))
    .closeOnJvmShutdown()
    .concurrencyDisable()
    .transactionEnable()
    .make()

fun main(args: Array<String>) {
    notice()

    val port = when (val arg = args.getOrNull(0)) {
        "-?", "/?", "-h", "/h", "--help", "/help" -> usage()
        else -> arg?.toIntOrNull(10) ?: 8080
    }
    println("Web API will be hosted on port $port")

    Master(port, DbWrapper(getDatabase()))
    // .run()
}
