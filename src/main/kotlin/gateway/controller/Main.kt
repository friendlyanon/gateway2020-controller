package gateway.controller

import gateway.controller.storage.Storage
import kotlin.system.exitProcess

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

fun main(args: Array<String>) {
    notice()

    val port = when (val arg = args.getOrNull(0)) {
        "-?", "/?", "-h", "/h", "--help", "/help" -> usage()
        else -> arg?.toIntOrNull(10) ?: 8080
    }
    println("Web API will be hosted on port $port")

    val localStorage =
        Storage("jdbc:h2:controller.h2.db", "controller", "secret")
    Master(port, localStorage).run()
}
