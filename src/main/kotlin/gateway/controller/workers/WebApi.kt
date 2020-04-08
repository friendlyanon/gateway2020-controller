package gateway.controller.workers

import fi.iki.elonen.NanoHTTPD
import gateway.controller.Master
import gateway.controller.events.Event
import gateway.controller.events.master.ApiReadyEvent
import gateway.controller.events.master.RestartEvent
import gateway.controller.server.BoundRunner
import gateway.controller.server.Server
import gateway.controller.utils.Queue
import java.util.concurrent.Executors

class WebApi(queue: Queue, port: Int) : AbstractWorker(queue) {
    private val server = Server(this, port).apply {
        setAsyncRunner(BoundRunner(Executors.newSingleThreadExecutor()))
    }

    fun put(event: Event) {
        require(Master.thread != Thread.currentThread())
        queue.put(event)
    }

    override fun run() {
        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            put(ApiReadyEvent())
        } catch (e: Throwable) {
            put(RestartEvent(exception = e))
        }
    }
}
