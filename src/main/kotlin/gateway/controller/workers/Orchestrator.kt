package gateway.controller.workers

import gateway.controller.Master
import gateway.controller.events.Event
import gateway.controller.utils.Queue

class Orchestrator(queue: Queue) : AbstractWorker(queue) {
    fun put(event: Event) {
        require(Master.thread != Thread.currentThread())
        queue.put(event)
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}
