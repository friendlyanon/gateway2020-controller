package gateway.controller.events.handlers

import gateway.controller.Master
import gateway.controller.events.Event
import gateway.controller.events.EventException
import gateway.controller.events.master.*
import gateway.controller.events.master.ConnectionRequestEvent.Type.LOCAL
import gateway.controller.events.master.ConnectionRequestEvent.Type.REMOTE
import gateway.controller.events.orchestrator.ConnectionEvent
import gateway.controller.events.webapi.StatusEvent

class MasterEventHandler(private val master: Master) {
    enum class Status {
        INITIAL;

        override fun toString() = name.toLowerCase()
    }

    val status = Status.INITIAL

    private val containers = mutableMapOf(
        master.webApi.name to master.webApi,
        master.orchestrator.name to master.orchestrator
    )

    fun onEvent(event: Event) = when (event) {
        is ApiRunningEvent -> onApiRunning(event)
        is ConnectionRequestEvent -> onConnectionRequest(event)
        is InquireStatusEvent -> onInquireStatus(event)
        is RestartEvent -> onRestart(event)
        is SettingsChangedEvent -> onSettingsChanged(event)
        else -> throw EventException("Not a master thread event", event)
    }

    private fun onInquireStatus(event: InquireStatusEvent) {
        event.port.offer(StatusEvent(status))
    }

    private fun onApiRunning(event: ApiRunningEvent) {
        TODO("Make sure we have the settings we need to run the orchestrator")
    }

    private fun onConnectionRequest(event: ConnectionRequestEvent) {
        val storage = when (event.type) {
            LOCAL -> master.localStorage
            REMOTE -> master.remoteStorage
        }
        event.port.offer(ConnectionEvent(storage.getConnection()))
    }

    private fun onSettingsChanged(event: SettingsChangedEvent) {
        TODO("Store the settings and start the orchestrator if necessary")
    }

    private fun onRestart(event: RestartEvent) {
        val name = event.name
        val container = containers[name]!!
        println("Thread $name requested a restart")

        // if the thread requested a restart, then it should have already
        // prepared to shut down, calling to interrupt only to make sure
        container.interrupt()

        container.restart()
        println("Thread $name was restarted")
    }
}
