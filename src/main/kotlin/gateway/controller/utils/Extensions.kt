package gateway.controller.utils

import gateway.controller.events.Event

inline fun <reified T : Event> Event.cast() = when (this) {
    is T -> this
    else -> null
}
