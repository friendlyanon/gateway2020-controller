package gateway.controller.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.iki.elonen.NanoHTTPD
import gateway.controller.workers.WebApi

class Server(val parent: WebApi) : NanoHTTPD(8080) {
    val mapper = jacksonObjectMapper()

    init {
        start(SOCKET_READ_TIMEOUT, false)
    }

    override fun serve(session: IHTTPSession): Response {
        try {
            super.serve(session)?.let { return it }
        } catch (e: Throwable) {
            return jsonResponse(status = Response.Status.INTERNAL_ERROR)
        }

        return jsonResponse(status = Response.Status.NOT_FOUND)
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    override fun serve(
        uri: String,
        method: Method,
        headers: Map<String, String>,
        params: Map<String, String>,
        files: Map<String, String>
    ): Response? {
        when (method) {
            Method.GET -> when (uri) {
                "/api/status" -> return getStatus()
            }
            Method.POST -> when (uri) {
                "/api/settings" -> return postSettings()
            }
        }

        return null
    }

    private fun getStatus(): Response {
        TODO("Not yet implemented")
    }

    private fun postSettings(): Response {
        TODO("Not yet implemented")
    }

    private fun jsonResponse(
        json: String = "{}",
        status: Response.Status = Response.Status.OK
    ) = newFixedLengthResponse(status, "application/json", json)
}
