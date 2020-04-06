package gateway.controller.events.orchestrator

import gateway.controller.events.Event
import java.sql.Connection

class ConnectionEvent(val connection: Connection) : Event
