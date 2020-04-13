package gateway.controller.workers

import gateway.controller.Master
import gateway.controller.events.Event
import gateway.controller.utils.Queue

abstract class AbstractWorker(private val queue: Queue) : Runnable {
    fun put(event: Event) {
        require(Master.thread != Thread.currentThread())
        queue.put(event)
    }
}
