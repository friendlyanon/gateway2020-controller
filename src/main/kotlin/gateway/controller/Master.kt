package gateway.controller

import gateway.controller.database.DbWrapper
import gateway.controller.database.SqlDatabase
import gateway.controller.events.MasterEventHandler
import gateway.controller.utils.InitOnceProperty.Companion.initOnce
import gateway.controller.utils.Queue
import gateway.controller.utils.getLogger
import gateway.controller.workers.Orchestrator
import gateway.controller.workers.WebApi
import gateway.controller.workers.WorkerContainer
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class Master(port: Int, val localStorage: DbWrapper) : Runnable {
    val webApi: WorkerContainer
    val orchestrator: WorkerContainer

    var remoteStorage: SqlDatabase by initOnce()

    private val eventSource = Queue(true)

    init {
        val q = eventSource
        webApi = WorkerContainer("WebApi") { WebApi(q, port) }
        orchestrator = WorkerContainer("Orchestrator") { Orchestrator(q) }
    }

    override fun run(): Nothing {
        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            LOG.info("Starting shutdown sequence")
            for (container in listOf(orchestrator, webApi)) {
                try {
                    container.interrupt()
                    container.join()
                } catch (e: InterruptedException) {
                }
            }
            LOG.info("Exiting")
        })

        // we only start the Web API's thread for now, then wait on it to start
        // running the web server
        webApi.restart()

        val handler = MasterEventHandler(this)
        try {
            while (true) {
                val event = eventSource.take()
                LOG.info("Event received {}", event)
                handler.onEvent(event)
            }
        } catch (e: Throwable) {
            LOG.error("Fatal exception in the main thread, shutting down", e)
            exitProcess(1)
        }
    }

    companion object {
        private val LOG = getLogger<Master>()

        private val mainThread = Thread.currentThread()
        val thread = object : Any() {
            override fun equals(other: Any?) = when (other) {
                is Thread -> other == mainThread
                else -> false
            }
        }
    }
}
