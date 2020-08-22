package gateway.controller.utils

class RingBuffer<T> : Iterable<T>, /* MutableList<T>, */ Cloneable {
    val capacity: Int
    var size: Int = 0
        private set
    private var tail: Int
    private val array: Array<Any?>
    private val head: Int
        get() = if (size == array.size) (tail + 1) % size else 0

    constructor(capacity: Int) {
        this.capacity = capacity
        array = arrayOfNulls(capacity)
        tail = -1
    }

    constructor(buffer: RingBuffer<T>) {
        this.capacity = buffer.capacity
        array = buffer.array.copyOf()
        tail = buffer.tail
    }

    fun add(item: T) {
        tail = (tail + 1) % capacity
        array[tail] = item
        if (size < capacity) {
            ++size
        }
    }

    fun toList() = iterator().asSequence().toList()

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int) = when {
        index !in 0 until size -> throw IndexOutOfBoundsException("$index")
        size == capacity -> array[(head + index) % capacity]
        else -> array[index]
    } as T

    override fun clone() = RingBuffer(this)

    override fun iterator() = RingBufferIterator()

    inner class RingBufferIterator : Iterator<T> {
        private var index = 0

        override fun hasNext() = index < size

        override fun next() = get(index++)
    }
}
