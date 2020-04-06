package gateway.controller.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.iki.elonen.NanoHTTPD
import gateway.controller.events.master.InquireStatusEvent
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.utils.Queue
import gateway.controller.workers.WebApi

class Server(private val parent: WebApi, port: Int) : NanoHTTPD(port) {
    val mapper = jacksonObjectMapper()

    override fun serve(session: IHTTPSession): Response {
        try {
            dispatch(session)?.let { return it }
        } catch (e: Throwable) {
            // TODO error logging maybe
            return jsonResponse(status = Response.Status.INTERNAL_ERROR)
        }

        return jsonResponse(status = Response.Status.NOT_FOUND)
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun dispatch(session: IHTTPSession): Response? {
        when (session.method) {
            Method.GET -> when (session.uri) {
                "/api/status" -> return getStatus()
            }
            Method.POST -> when (session.uri) {
                "/api/settings" -> return postSettings(parseBody(session))
            }
        }

        return null
    }

    private fun getStatus(): Response {
        val queue = Queue()
        parent.offer(InquireStatusEvent(queue))
        val event = queue.take() as StatusEvent
        return jsonResponse("""{"status":"${event.status}"}""")
    }

    private fun postSettings(json: String): Response {
        TODO("Not yet implemented")
    }

    private fun jsonResponse(
        json: String = "{}",
        status: Response.Status = Response.Status.OK
    ) = newFixedLengthResponse(status, "application/json", json)

    private fun parseBody(session: IHTTPSession): String {
        val stream = session.inputStream
        TODO()
    }
}
