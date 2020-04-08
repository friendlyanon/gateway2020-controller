package gateway.controller.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.iki.elonen.NanoHTTPD
import gateway.controller.events.EventException
import gateway.controller.events.master.CommandEvent
import gateway.controller.events.master.InquireStatusEvent
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.server.models.Command as CommandModel
import gateway.controller.utils.Queue
import gateway.controller.utils.cast
import gateway.controller.workers.WebApi

class Server(private val parent: WebApi, port: Int) : NanoHTTPD(port) {
    private val mapper = jacksonObjectMapper()

    override fun serve(session: IHTTPSession): Response {
        try {
            dispatch(session)?.let { return it }
        } catch (e: Throwable) {
            // TODO log the error
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
                "/api/command" -> return postCommand(parseBody(session))
            }
        }

        return null
    }

    private fun postCommand(json: String): Response {
        val (command) = json.parseAs<CommandModel>()
        val type = Command.valueOf(command)
        parent.put(CommandEvent(type))
        return jsonResponse()
    }

    private fun getStatus(): Response {
        val queue = Queue()
        parent.put(InquireStatusEvent(queue))
        val event = queue.take()
        return when (val status = event.cast<StatusEvent>()) {
            null -> throw EventException("Not a status event", event)
            else -> jsonResponse("""{"status":"${status.status}"}""")
        }
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

    private inline fun <reified T> String.parseAs() =
        mapper.readValue(this, T::class.java)
}
