package gateway.controller.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.iki.elonen.NanoHTTPD
import gateway.controller.events.EventException
import gateway.controller.events.master.CommandEvent
import gateway.controller.events.master.InquireStatusEvent
import gateway.controller.events.master.SettingsChangedEvent
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.server.models.Settings
import gateway.controller.utils.Queue
import gateway.controller.utils.cast
import gateway.controller.utils.getLogger
import gateway.controller.workers.WebApi
import gateway.controller.server.models.Command as CommandModel

class Server(private val parent: WebApi, port: Int) : NanoHTTPD(port) {
    private val mapper = jacksonObjectMapper()

    override fun serve(session: IHTTPSession): Response {
        try {
            super.serve(session)?.let { return it }
        } catch (e: Throwable) {
            LOG.error("Error handling a request", e)
            return jsonResponse(status = Response.Status.INTERNAL_ERROR)
        }

        return jsonResponse(status = Response.Status.NOT_FOUND)
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun serve(
        uri: String,
        method: Method,
        headers: Map<String, String>,
        parms: Map<String, String>,
        files: Map<String, String>
    ): Response? {
        LOG.info("Incoming request: {} {}", method, uri)
        val body = files.getValue("postData")
        when (method) {
            Method.GET -> when (uri) {
                "/api/status" -> return getStatus()
            }
            Method.POST -> when (uri) {
                "/api/settings" -> return postSettings(body)
                "/api/command" -> return postCommand(body)
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
        val (settings) = json.parseAs<Settings>()
        parent.put(SettingsChangedEvent(settings))
        return jsonResponse()
    }

    private fun jsonResponse(
        json: String = "{}",
        status: Response.Status = Response.Status.OK
    ) = newFixedLengthResponse(status, "application/json", json)

    private inline fun <reified T> String.parseAs() =
        mapper.readValue(this, T::class.java)

    companion object {
        private val LOG = getLogger<Server>()
    }
}
