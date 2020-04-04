package gateway.controller

class Event {
    val thread: Thread = Thread.currentThread()

    operator fun component1() = thread
}
