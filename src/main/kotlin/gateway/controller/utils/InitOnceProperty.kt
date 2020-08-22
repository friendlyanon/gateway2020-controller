package gateway.controller.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class InitOnceProperty<T> : ReadWriteProperty<Any, T> {
    private var value: Any? = EMPTY

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value === EMPTY) {
            throw IllegalStateException("Value isn't initialized")
        }
        return value as T
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (this.value !== EMPTY) {
            throw IllegalStateException("Value is initialized")
        }
        this.value = value
    }

    companion object {
        private object EMPTY

        fun <T> initOnce(): ReadWriteProperty<Any, T> = InitOnceProperty()
    }
}
