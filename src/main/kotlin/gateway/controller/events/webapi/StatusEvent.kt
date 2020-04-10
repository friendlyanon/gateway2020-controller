package gateway.controller.events.webapi

import gateway.controller.events.Event
import gateway.controller.events.MasterEventHandler

class StatusEvent(val status: MasterEventHandler.Status) : Event()
