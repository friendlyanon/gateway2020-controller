package gateway.controller.events.master

import gateway.controller.events.Event
import gateway.controller.server.Command

class CommandEvent(val type: Command) : Event
