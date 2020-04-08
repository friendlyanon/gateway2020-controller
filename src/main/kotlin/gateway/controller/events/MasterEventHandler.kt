package gateway.controller.events

import gateway.controller.Master
import gateway.controller.database.DbWrapper.Table.DETAILS
import gateway.controller.events.master.*
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.server.Command
import gateway.controller.storage.Storage
import gateway.controller.utils.simpleName
import org.slf4j.LoggerFactory

class MasterEventHandler(private val master: Master) {
    // TODO add more statuses
    enum class Status {
        INITIAL,
        WAITING_FOR_DSN;

        override fun toString() = name.toLowerCase()
    }

    private var status = Status.INITIAL

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
        LOG.info("Web API is ready")
        val dbDsn = master.localStorage.readOnlyUse(DETAILS) {
            return@readOnlyUse it.getOrDefault("dbDsn", null)
        }
        if (dbDsn == null) {
            LOG.info("Controller is not yet registered from a web interface")
            status = Status.WAITING_FOR_DSN
        } else {
            LOG.info("DSN found, starting orchestrator")
            master.remoteStorage = Storage(dbDsn)
            master.orchestrator.start()
        }
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
        LOG.info("Thread $name requested a restart")

        // if the thread requested a restart, then it should have already
        // prepared to shut down, calling to interrupt only to make sure
        container.interrupt()

        container.restart()
        LOG.info("Thread $name was restarted")
    }

    companion object {
        private val LOG =
            LoggerFactory.getLogger(simpleName<MasterEventHandler>())
    }
}
