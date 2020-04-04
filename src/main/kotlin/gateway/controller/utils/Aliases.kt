package gateway.controller.utils

import gateway.controller.Event
import java.util.concurrent.SynchronousQueue

typealias Queue = SynchronousQueue<Event>
typealias ThreadFactory = (Queue) -> Thread
