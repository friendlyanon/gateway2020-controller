package gateway.controller.events

class RestartEvent private constructor(
    val name: String = Thread.currentThread().name
) : Event
