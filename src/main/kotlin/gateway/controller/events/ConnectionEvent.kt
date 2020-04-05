package gateway.controller.events

import java.sql.Connection

class ConnectionEvent(val connection: Connection) : Event
