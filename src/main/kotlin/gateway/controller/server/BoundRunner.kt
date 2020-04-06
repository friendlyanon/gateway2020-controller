package gateway.controller.server

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.AsyncRunner
import java.util.*
import java.util.concurrent.ExecutorService

class BoundRunner(private val executorService: ExecutorService) : AsyncRunner {
    private val running =
        Collections.synchronizedList(ArrayList<NanoHTTPD.ClientHandler>())

    override fun closeAll() {
        // copy of the list for concurrency
        for (clientHandler in ArrayList(running)) {
            clientHandler.close()
        }
    }

    override fun closed(clientHandler: NanoHTTPD.ClientHandler) {
        running.remove(clientHandler)
    }

    override fun exec(clientHandler: NanoHTTPD.ClientHandler) {
        executorService.submit(clientHandler)
        running.add(clientHandler)
    }
}
