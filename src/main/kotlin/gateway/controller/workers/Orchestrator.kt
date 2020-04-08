package gateway.controller.workers

import gateway.controller.Master
import gateway.controller.events.Event
import gateway.controller.utils.Queue

// TODO
//  when modules request key-value data to be stored, the actual stored key
//  should follow the pattern below:
//    <module_folder>/<key>
//  Assuming <module_folder> is "reader" and <key> is "lastId", when reading
//  them out should look like this:
//    val readerLastId: String? = localStorage.readOnlyUse(STORE) {
//        getOrDefault("reader/lastId", null)
//    }
class Orchestrator(queue: Queue) : AbstractWorker(queue) {
    fun put(event: Event) {
        require(Master.thread != Thread.currentThread())
        queue.put(event)
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}
