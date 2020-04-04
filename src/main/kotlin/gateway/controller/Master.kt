package gateway.controller

import gateway.controller.utils.Queue
import gateway.controller.utils.RingBuffer
import gateway.controller.utils.ThreadFactory

class Master : Runnable {
    private val threadMap = mutableMapOf<Thread, ThreadFactory>()

    // we keep track of the times when threads request a restart and if this
    // happens too many times in a given period, then we handle that
    private val dateMap = mutableMapOf<ThreadFactory, RingBuffer<Long>>()

    // source of events to know when to restart which thread
    private val eventSource = Queue(true)

    override fun run() {
        arrayOf<ThreadFactory>(
            { Thread(WebApi(it), "WebApi") },
            { Thread(Orchestrator(it), "Orchestrator") }
        ).map {
            dateMap[it] = RingBuffer(dateSampleSize)
            it(eventSource).apply { threadMap[this] = it }
        }.forEach { it.start() }

        while (true) {
            onRestartEvent(eventSource.take())
        }
    }

    private fun onRestartEvent(event: Event) {
        val (thread) = event
        println("Thread ${thread.name} requested a restart")

        // if the thread requested a restart, then it should have already
        // prepared to shut down, calling to interrupt only to make sure
        thread.interrupt()

        // events should come only from threads that are in the map, so these
        // can't be null
        val factory = threadMap.remove(thread)!!
        val dateBuffer = dateMap[factory]!!
        if (dateBuffer.size == dateSampleSize) {
            val difference = dateBuffer[dateSampleSize - 1] - dateBuffer[0]
            if (difference < restartPeriodMillis) {
                TODO("Too many restarts requested, something is wrong")
            }
        }

        factory(eventSource).also { threadMap[it] = factory }.apply {
            start()
            println("Thread $name was restarted")
        }
    }

    companion object {
        // 5 restarts in 30 seconds might be indicative of an error
        const val dateSampleSize = 5
        const val restartPeriodMillis = 30_000
    }
}
