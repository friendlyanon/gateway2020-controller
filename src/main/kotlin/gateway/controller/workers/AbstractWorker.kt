package gateway.controller.workers

import gateway.controller.utils.Queue

abstract class AbstractWorker(protected val queue: Queue) : Runnable
