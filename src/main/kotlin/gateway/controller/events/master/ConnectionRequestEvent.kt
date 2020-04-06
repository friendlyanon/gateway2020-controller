package gateway.controller.events.master

import gateway.controller.events.Event
import gateway.controller.utils.Queue

class ConnectionRequestEvent(val port: Queue, val type: Type) : Event {
    enum class Type { LOCAL, REMOTE }
}
