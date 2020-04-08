package gateway.controller.events.master

import gateway.controller.events.Event

class RestartEvent(val name: String = Thread.currentThread().name) : Event()
