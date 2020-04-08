package gateway.controller

import gateway.controller.database.DbWrapper
import gateway.controller.events.MasterEventHandler
import gateway.controller.storage.Storage
import gateway.controller.utils.InitOnceProperty.Companion.initOnce
import gateway.controller.utils.Queue
import gateway.controller.workers.Orchestrator
import gateway.controller.workers.WebApi
import gateway.controller.workers.WorkerContainer
import kotlin.system.exitProcess

class Master(port: Int, val localStorage: DbWrapper) : Runnable {
    val webApi: WorkerContainer
    val orchestrator: WorkerContainer

    var remoteStorage: Storage by initOnce()

    private val eventSource = Queue(true)

    init {
        val q = eventSource
        webApi = WorkerContainer("WebApi") { WebApi(q, port) }
        orchestrator = WorkerContainer("Orchestrator") { Orchestrator(q) }
    }

    override fun run(): Nothing {
        orchestrator.initThread()

        // we only start the Web API's thread for now, then wait on it to start
        // running the web server
        webApi.restart()

        val handler = MasterEventHandler(this)
        try {
            while (true) {
                handler.onEvent(eventSource.take())
            }
        } catch (e: Throwable) {
            System.err.println("Fatal exception in the main thread, shutting down")
            e.printStackTrace(System.err)
            exitProcess(1)
        }
    }

    companion object {
        private val mainThread = Thread.currentThread()
        val thread = object : Any() {
            override fun equals(other: Any?) = when (other) {
                is Thread -> other == mainThread
                else -> throw IllegalArgumentException("Expected a thread")
            }
        }
    }
}
