package gateway.controller.utils

import gateway.controller.events.Event
import gateway.controller.workers.AbstractWorker
import java.util.concurrent.SynchronousQueue

typealias Queue = SynchronousQueue<Event>
typealias WorkerFactory = () -> AbstractWorker
