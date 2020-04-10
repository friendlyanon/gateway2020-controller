package gateway.controller.events.master

import gateway.controller.events.Event

class RestartEvent(val exception: Throwable? = null) : Event() {
    val name: String = Thread.currentThread().name
}
