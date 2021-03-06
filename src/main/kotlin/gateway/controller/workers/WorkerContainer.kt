package gateway.controller.workers

import gateway.controller.utils.RingBuffer
import gateway.controller.utils.WorkerFactory

class WorkerContainer(
    val name: String,
    private val factory: WorkerFactory
) {
    private var thread = Thread(factory(), name)
    private val dateBuffer = RingBuffer<Long>(dateSampleSize)

    fun interrupt() {
        thread.interrupt()

        dateBuffer.add(System.currentTimeMillis())
        checkDates()
    }

    fun join() {
        thread.join()
    }

    fun start() {
        thread.start()
    }

    fun restart() {
        thread = Thread(factory(), name)
        start()
    }

    private fun checkDates() {
        if (dateBuffer.size != dateSampleSize) {
            return
        }

        val difference = dateBuffer[dateSampleSize - 1] - dateBuffer[0]
        if (difference >= restartPeriodMillis) {
            return
        }

        TODO("Handle a thread restarting too many times too quickly")
    }

    companion object {
        // 5 restarts in 30 seconds might be indicative of an error
        const val dateSampleSize = 5
        const val restartPeriodMillis = 30_000
    }
}
