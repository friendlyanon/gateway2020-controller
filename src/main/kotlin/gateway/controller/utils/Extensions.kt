package gateway.controller.utils

import gateway.controller.events.Event

inline fun <reified T : Event> Event.cast(): T? =
    try {
        this as T
    } catch (unused: ClassCastException) {
        null
    }
