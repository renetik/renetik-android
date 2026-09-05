package renetik.android.core.kotlin

import renetik.android.core.java.lang.createInstance
import kotlin.reflect.KClass

fun <T> Any.privateField(name: String): T? = runCatching {
    val field = this::class.java.getDeclaredField(name)
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    return field.get(this) as T
}.logError().getOrNull()

fun <T> Any.setPrivateField(name: String, fieldValue: T) =
    setPrivateField(this.kClass, name, fieldValue)

fun <V : Any, T> V.setPrivateField(type: KClass<V>, name: String, fieldValue: T) = runCatching {
    val field = type.java.getDeclaredField(name)
    field.isAccessible = true
    field.set(this, fieldValue)
}.logError()

fun setStaticField(type: KClass<*>, name: String, value: Any?) = runCatching {
    val field = type.java.getDeclaredField(name)
    field.isAccessible = true
    field.set(null, value)
}.logError()

@Suppress("UNCHECKED_CAST")
fun <T> createClass(className: String): Class<T>? = runCatching {
    Class.forName(className) as? Class<T>
}.logError().getOrNull()

fun classExist(name: String): Boolean = runCatching {
    val loader = Thread.currentThread().contextClassLoader
        ?: ClassLoader.getSystemClassLoader()
    Class.forName(name, false, loader)
}.isSuccess

fun <T> createInstance(className: String): T? = createClass<T>(className)?.createInstance()

@Suppress("UNCHECKED_CAST")
fun <T> createInstance(className: String, vararg arguments: Any): T? =
    createClass<T>(className)?.constructors?.firstOrNull {
        it.parameterTypes.size == arguments.size
    }?.newInstance(*arguments) as? T

fun <T> Class<T>?.invoke(function: String, argument: T? = null): Any? =
    this?.getMethod(function)?.invoke(argument)

fun Any.invokeFunction(name: String, argument: Any? = null): Any? = runCatching {
    javaClass.getMethod(name).also { it.isAccessible = true }.let { method ->
        argument?.let { method.invoke(this, it) } ?: method.invoke(this)
    }
}.getOrNull()

fun Any.invokeFunction(name: String, vararg argument: Any): Any? = runCatching {
    javaClass.getMethod(name).also { it.isAccessible = true }.invoke(this, argument)
}.getOrNull()

val <T : Any> T.kClass: KClass<T> get() = javaClass.kotlin