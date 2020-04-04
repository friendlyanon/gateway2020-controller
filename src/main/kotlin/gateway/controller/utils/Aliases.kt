package gateway.controller.utils

import java.util.concurrent.SynchronousQueue

typealias Queue = SynchronousQueue<Event>
typealias ThreadFactory = (Queue) -> Thread
