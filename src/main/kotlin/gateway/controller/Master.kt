package gateway.controller

import gateway.controller.events.handlers.MasterEventHandler
import gateway.controller.storage.Storage
import gateway.controller.utils.Queue
import gateway.controller.workers.Orchestrator
import gateway.controller.workers.WebApi
import gateway.controller.workers.WorkerContainer

class Master(val localStorage: Storage) : Runnable {
    val webApi: WorkerContainer
    val orchestrator: WorkerContainer

    lateinit var remoteStorage: Storage

    // source of events to know when to restart which thread
    private val eventSource = Queue(true)

    init {
        val q = eventSource
        webApi = WorkerContainer("WebApi") { WebApi(q) }
        orchestrator = WorkerContainer("Orchestrator") { Orchestrator(q) }
    }

    override fun run(): Nothing {
        orchestrator.initThread()

        // we only start the Web API's thread for now, then wait on it to start
        // running the web server
        webApi.restart()

        val handler = MasterEventHandler(this)
        while (true) {
            handler.onEvent(eventSource.take())
        }
    }
}
