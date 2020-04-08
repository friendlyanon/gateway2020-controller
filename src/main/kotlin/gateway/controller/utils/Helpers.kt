package gateway.controller.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun getClassName(name: String) =
    name.substring(name.lastIndexOf('.') + 1, name.length)

inline fun <reified T> simpleName() = getClassName(T::class.java.name)

fun simpleName(clazz: Class<*>) = getClassName(clazz.name)

inline fun <reified T> getLogger(): Logger =
    LoggerFactory.getLogger(simpleName<T>())
