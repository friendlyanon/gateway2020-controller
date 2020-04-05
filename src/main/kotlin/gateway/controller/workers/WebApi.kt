package gateway.controller.workers

import fi.iki.elonen.NanoHTTPD
import gateway.controller.server.Server
import gateway.controller.utils.InitOnceProperty.Companion.initOnce
import gateway.controller.utils.Queue

class WebApi(queue: Queue) : AbstractWorker(queue) {
    var server: NanoHTTPD by initOnce()

    override fun run() {
        server = Server(this)
    }
}
