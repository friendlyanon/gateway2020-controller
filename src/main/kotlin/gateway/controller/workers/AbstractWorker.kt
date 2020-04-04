package gateway.controller.workers

import gateway.controller.utils.Queue

abstract class AbstractWorker(private val queue: Queue) : Runnable
