package gateway.controller.utils

fun getClassName(name: String) =
    name.substring(name.lastIndexOf('.') + 1, name.length)

inline fun <reified T> simpleName() = getClassName(T::class.java.name)

fun simpleName(clazz: Class<*>) = getClassName(clazz.name)
