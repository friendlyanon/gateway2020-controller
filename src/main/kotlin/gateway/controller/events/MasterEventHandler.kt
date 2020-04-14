package gateway.controller.events

import gateway.controller.Master
import gateway.controller.database.DbWrapper.Table.DETAILS
import gateway.controller.database.SqlDatabase
import gateway.controller.events.master.*
import gateway.controller.events.master.DbRequestEvent.Type
import gateway.controller.events.webapi.StatusEvent
import gateway.controller.server.Command
import gateway.controller.utils.StateBlock
import gateway.controller.utils.generateIterator
import gateway.controller.utils.getLogger

class MasterEventHandler(private val master: Master) {
    // TODO add more statuses
    enum class Status {
        INITIAL,
        WAITING_FOR_DB_URL,
        WAITING_FOR_GATEWAY_ID,
        WAITING_FOR_ORCHESTRATOR_SETTINGS,
        STARTING_ORCHESTRATOR,
        WAITING_FOR_DATA,
        PROCESSING_DATA;

        override fun toString() = name.toLowerCase()
    }

    private var status = Status.INITIAL

    private val containers = mutableMapOf(
        master.webApi.name to master.webApi,
        master.orchestrator.name to master.orchestrator
    )

    fun onEvent(event: Event) = when (event) {
        is ApiReadyEvent -> onApiReady()
        is CommandEvent -> onCommand(event)
        is DbRequestEvent -> onDbRequest(event)
        is InquireStatusEvent -> onInquireStatus(event)
        is RestartEvent -> onRestart(event)
        is SettingsChangedEvent -> onSettingsChanged(event)
        else -> throw EventException("Not a master event", event)
    }

    private fun onCommand(event: CommandEvent) = when (event.type) {
        Command.RESTART -> master.orchestrator.restart()
        Command.SHUTDOWN -> master.orchestrator.interrupt()
    }

    private fun onInquireStatus(event: InquireStatusEvent) {
        event.port.put(StatusEvent(status))
    }

    private fun onApiReady() {
        LOG.info("Web API is ready")
        runStateMachine()
    }

    private fun onDbRequest(event: DbRequestEvent) = when (event.type) {
        Type.LOCAL -> event.port.put(master.localStorage)
        Type.REMOTE -> event.port.put(master.remoteStorage)
    }

    private fun onSettingsChanged(event: SettingsChangedEvent) {
        master.localStorage.readWriteUse(DETAILS) {
            for ((key, value) in event.settings) {
                put(key, value)
            }
        }
        runStateMachine()
    }

    private fun onRestart(event: RestartEvent) {
        val name = event.name
        val container = containers[name]!!

        val exception = event.exception
        if (exception != null) {
            val msg = "Restarting thread $name, because it threw an exception"
            LOG.error(msg, exception)
        } else {
            LOG.info("Thread $name requested a restart")
        }

        // if the thread requested a restart, then it should have already
        // prepared to shut down, calling to interrupt only to make sure
        container.interrupt()

        container.restart()
        LOG.info("Thread $name was restarted")
    }

    private fun runStateMachine(): Boolean {
        handler = this
        return stateMachine.next()
    }

    companion object {
        private val LOG = getLogger<MasterEventHandler>()

        private var handler: MasterEventHandler? = null
        private val stateMachine: Iterator<Boolean> = { block: StateBlock ->
            generateIterator {
                val handler = handler!!
                MasterEventHandler.handler = null
                handler.block()
            }
        } block@{
            fun detailsGet(key: String) =
                master.localStorage.readOnlyUse(DETAILS) {
                    getOrDefault(key, null)
                }

            if (status == Status.INITIAL) {
                status = Status.WAITING_FOR_DB_URL
            }

            if (status == Status.WAITING_FOR_DB_URL) {
                val dbUrl = detailsGet("dbUrl")
                if (dbUrl == null) {
                    LOG.info("Controller is not yet registered from a web interface")
                    return@block false
                }

                LOG.info("Database URL found")
                master.remoteStorage = SqlDatabase("jdbc:$dbUrl")
                status = Status.WAITING_FOR_GATEWAY_ID
            }

            if (status == Status.WAITING_FOR_GATEWAY_ID) {
                val gatewayId = detailsGet("gatewayId")
                if (gatewayId == null) {
                    LOG.info("Web interface has not yet sent the gateway ID")
                    return@block false
                }

                LOG.info("Gateway ID found: {}", gatewayId)
                status = Status.WAITING_FOR_ORCHESTRATOR_SETTINGS
            }

            if (status == Status.WAITING_FOR_ORCHESTRATOR_SETTINGS) {
                val modulesOrder = detailsGet("modulesOrder")
                if (modulesOrder == null) {
                    LOG.info("The order of modules was not yet set up")
                    return@block false
                }

                LOG.info("Module order found, starting orchestrator")
                master.orchestrator.start()
                status = Status.STARTING_ORCHESTRATOR
            }

            return@block true
        }
    }
}
