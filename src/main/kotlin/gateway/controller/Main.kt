package gateway.controller

import gateway.controller.storage.Storage
import kotlin.system.exitProcess

private fun usage(): Nothing {
    println()
    exitProcess(0)
}

fun main(args: Array<String>) {
    var port = 8080
    if (args.isNotEmpty()) {
        when (val arg = args[0]) {
            "-?", "/?", "-h", "/h", "--help", "/help" -> usage()
            else -> arg.toIntOrNull(10)?.let { port = it }
        }
    }
    println("Web API will be hosted on port $port")
    Master(port, Storage("jdbc:h2:~/test", "controller", "secret")).run()
}
