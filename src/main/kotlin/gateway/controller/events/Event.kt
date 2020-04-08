package gateway.controller.events

import gateway.controller.utils.simpleName

abstract class Event {
    override fun toString(): String = simpleName(this::class.java)
}
