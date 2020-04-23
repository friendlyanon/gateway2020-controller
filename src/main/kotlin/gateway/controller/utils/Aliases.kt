package gateway.controller.utils

import gateway.controller.events.Event
import gateway.controller.events.MasterEventHandler
import gateway.controller.workers.AbstractWorker
import java.util.concurrent.SynchronousQueue

// Array of size 2 arrays [[key, value], [key, value], ...]
typealias SettingsList = List<List<String>>

typealias Queue = SynchronousQueue<Event>
typealias WorkerFactory = () -> AbstractWorker
typealias StateBlock = MasterEventHandler.() -> Boolean
typealias DbMap = HashMap<String, Any?>
