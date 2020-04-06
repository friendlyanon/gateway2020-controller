package gateway.controller.events

class EventException(
    s: String,
    val event: Event
) : IllegalArgumentException(s) {
    override val message: String
        get() = "${super.message} $event"
}
