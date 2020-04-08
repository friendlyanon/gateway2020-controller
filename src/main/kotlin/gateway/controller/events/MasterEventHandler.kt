package gateway.controller.events

import gateway.controller.Master
import gateway.controller.events.master.*
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.server.Command

class MasterEventHandler(private val master: Master) {
    // TODO add more statuses
    enum class Status {
        INITIAL;

        override fun toString() = name.toLowerCase()
    }

    private val status = Status.INITIAL

    private val containers = mutableMapOf(
        master.webApi.name to master.webApi,
        master.orchestrator.name to master.orchestrator
    )

    fun onEvent(event: Event) = when (event) {
        is ApiReadyEvent -> onApiReady(event)
        is CommandEvent -> onCommand(event)
        is DbRequestEvent -> onDbRequest(event)
        is InquireStatusEvent -> onInquireStatus(event)
        is RestartEvent -> onRestart(event)
        is SettingsChangedEvent -> onSettingsChanged(event)
        else -> throw EventException("Not a master event", event)
    }

    private fun onCommand(event: CommandEvent) = when (event.type) {
        Command.restart -> master.orchestrator.restart()
        Command.shutdown -> master.orchestrator.interrupt()
    }

    private fun onInquireStatus(event: InquireStatusEvent) {
        event.port.put(StatusEvent(status))
    }

    private fun onApiReady(event: ApiReadyEvent) {
        TODO("Make sure we have the settings we need to run the orchestrator")
    }

    private fun onDbRequest(event: DbRequestEvent) {
        TODO()
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
