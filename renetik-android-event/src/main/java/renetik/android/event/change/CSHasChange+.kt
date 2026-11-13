@file:OptIn(ExperimentalAtomicApi::class)
@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.event.change

import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.suspendCancellableCoroutine
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.ArgFun
import renetik.android.core.lang.Fun
import renetik.android.core.lang.tuples.CSQuadruple
import renetik.android.event.CSEvent
import renetik.android.event.change.CSHasChange.Companion.action
import renetik.android.event.dispatch.CSDebouncer.Companion.debouncer
import renetik.android.event.dispatch.JobRegistration
import renetik.android.event.dispatch.launch
import renetik.android.event.fire
import renetik.android.event.invoke
import renetik.android.event.registration.*
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

operator fun <T> CSHasChange<T>.invoke(param: (T) -> Unit) = onChange(param)

operator fun CSHasChange<Unit>.invoke(param: () -> Unit) = onChange(param)

fun CSHasChange<Boolean>.onTrue(function: () -> Unit): CSRegistration =
    onChange { param -> if (param) function() }

fun CSHasChange<Boolean>.onFalse(function: () -> Unit): CSRegistration =
    onChange { param -> if (!param) function() }

fun <T> CSHasChange<T>.onChangeLaunch(
    dispatcher: CoroutineContext = Main,
    function: suspend (T) -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + onChange { param -> it.launch(dispatcher) { function(param) } }
}

fun <T> CSHasChange<T>.onChangeLaunch(
    key: String, dispatcher: CoroutineContext = Main,
    function: suspend (T) -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + onChange { param -> it.launch(key, dispatcher) { function(param) } }
}

fun <T> CSHasChange<T>.actionLaunch(
    dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + action { it.launch(dispatcher) { function() } }
}

fun <T> CSHasChangeValue<T>.onChangeFromToLaunch(
    function: suspend (from: T, to: T) -> Unit,
): CSRegistration {
    var value = this.value
    return onChangeLaunch { function(value, it); value = it }
}

fun <T> CSHasChange<T>.onChangeLaunch(
    parent: CSHasRegistrations, function: suspend (T) -> Unit
): CSRegistration {
    var previous: JobRegistration? = null
    var current: JobRegistration? = null
    val onChangeRegistration = onChange { param ->
        current = parent.launch { registration ->
            previous?.waitToFinish()
            previous = registration
            function(param)
        }
    }
    return CSRegistration(true, onCancel = {
        onChangeRegistration.cancel()
        current?.cancel()
        previous?.cancel()
    })
}

fun <T> CSHasChangeValue<T>.onChangeFromToLaunch(
    parent: CSHasRegistrations, function: suspend (from: T, to: T) -> Unit,
): CSRegistration {
    var value = this.value
    return onChangeLaunch(parent) { function(value, it); value = it }
}

fun <T> CSHasChangeValue<T>.actionLaunch(
    parent: CSHasRegistrations,
    dispatcher: CoroutineContext = Main,
    function: suspend (T) -> Unit,
): CSRegistration {
    var previous: JobRegistration? = null
    var current: JobRegistration? = null
    val onChangeRegistration = action { param ->
        current = parent.launch(dispatcher) { registration ->
            previous?.waitToFinish()
            previous = registration
            function(param)
        }
    }
    return CSRegistration(true, onCancel = {
        onChangeRegistration.cancel()
        current?.cancel()
        previous?.cancel()
    })
}

fun CSHasChange<Boolean>.onTrueLaunch(
    dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + onTrue { it.launch(dispatcher) { function() } }
}

fun CSHasChange<Boolean>.onFalseLaunch(
    dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + onFalse { it.launch(dispatcher) { function() } }
}

fun CSHasChange<Boolean>.onFalseLaunch(
    key: String, dispatcher: CoroutineContext = Main,
    function: suspend () -> Unit
): CSRegistration = CSRegistrationsMap(debugClassName).also {
    it + onFalse { it.launch(key, dispatcher) { function() } }
}

suspend fun <T> CSHasChange<T>.wait(): T =
    suspendCancellableCoroutine { continuation ->
        val registration = AtomicReference<CSRegistration?>(null)
        val isDone = AtomicBoolean(false)
        val listener: (T) -> Unit = { value ->
            if (isDone.compareAndSet(expectedValue = false, newValue = true)) {
                continuation.resume(value)
                registration.exchange(null)?.cancel()
            }
        }
        registration.store(onChange(listener))
        if (isDone.load()) registration.exchange(null)?.cancel()
        continuation.invokeOnCancellation {
            if (isDone.compareAndSet(expectedValue = false, newValue = true))
                registration.exchange(null)?.cancel()
        }
    }

inline fun <Argument> CSHasChange<Argument>.onChange(
    crossinline function: () -> Unit,
): CSRegistration = onChange { _ -> function() }

inline fun CSHasChange<Unit>.action(
    crossinline function: () -> Unit,
): CSRegistration {
    function()
    return onChange(function)
}

inline fun <Argument> CSHasChange<Argument>.onChange(
    crossinline function: (CSRegistration, Argument) -> Unit,
): CSRegistration {
    lateinit var registration: CSRegistration
    registration = onChange { function(registration, it) }
    return registration
}

inline fun <Argument> CSHasChange<Argument>.onChange(
    crossinline function: (CSRegistration) -> Unit,
): CSRegistration {
    lateinit var registration: CSRegistration
    registration = onChange { function(registration) }
    return registration
}

/**
 * Registers a one-time change listener.
 *
 * @return [CSRegistration] of this listener. The registration is self-cancelled
 * on first change and must NOT be added to [CSHasRegistrations].
 */
inline fun <Argument> CSHasChange<Argument>.onChangeOnce(
    parent: CSHasRegistrations? = null,
    crossinline function: () -> Unit,
): CSRegistration {
    lateinit var registration: CSRegistration
    registration = onChange {
        registration.cancel()
        function()
    }.registerTo(parent)
    return registration
}

/**
 * Registers a one-time change listener.
 *
 * @return [CSRegistration] of this listener. The registration is self-cancelled
 * on first change and must NOT be added to [CSHasRegistrations].
 */
inline fun <Argument> CSHasChange<Argument>.onChangeOnce(
    parent: CSHasRegistrations? = null,
    crossinline function: (Argument) -> Unit,
) {
    var registration: CSRegistration? = null
    registration = onChange { argument ->
        registration?.cancel()
        function(argument)
    }.registerTo(parent)
}

inline fun <Argument> CSHasChange<out Argument?>.onChangeNotNullOnce(
    crossinline function: (Argument) -> Unit,
): CSRegistration = onChange { registration, argument ->
    if (argument != null) {
        registration.cancel()
        function(argument)
    }
}

inline fun <Argument> CSHasChangeValue<out Argument?>.actionNotNullOnce(
    crossinline function: (Argument) -> Unit,
): CSRegistration? = value?.let { function(it); return null } ?: onChangeNotNullOnce(function)

inline fun <Argument> CSHasChange<Argument>.onChangeLaterOnce(
    after: Duration,
    crossinline function: Fun,
): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    val laterOnceFunction = registrations.debouncer(after) { function() }
    registrations.register(onChange { laterOnceFunction() })
    return registrations
}

inline fun <Argument> CSHasChangeValue<Argument>.onChangeLaterOnce(
    after: Duration = ZERO,
    crossinline onChange: ArgFun<Argument>,
): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    var value1: Argument? = null
    val laterOnceFunction = registrations.debouncer(after) {
        if (registrations.isActive) onChange(value1 ?: value)
        value1 = null
    }
    registrations + onChange { value1 = it; laterOnceFunction() }
    return registrations
}

inline fun <Argument> CSHasChangeValue<Argument>.actionLaterOnce(
    after: Duration = ZERO,
    crossinline onChange: ArgFun<Argument>,
): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    var value1: Argument? = null
    val laterOnceFunction = registrations.debouncer(after) {
        if (registrations.isActive) onChange(value1 ?: value)
        value1 = null
    }
    registrations + onChange { value1 = it; laterOnceFunction() }
    laterOnceFunction()
    return registrations
}

inline fun <Argument> CSHasChange<Argument>.onChangeLaterOnce(
    crossinline function: Fun,
) = onChangeLaterOnce(ZERO, function)

inline fun CSHasChange<*>.eventLaterOnce(): CSEvent<*> = CSEvent.event().also {
    onChangeLaterOnce { it() }
}

inline fun CSHasChange<*>.event(): CSEvent<*> = CSEvent.event().also {
    onChange { it() }
}

fun Pair<CSHasChange<*>, CSHasChange<*>>.onChange(
    onChange: () -> Unit
): CSRegistration = CSRegistration(
    first.onChange(onChange), second.onChange(onChange)
)

fun Pair<CSHasChange<*>, CSHasChange<*>>.event(
    parent: CSHasRegistrations? = null
): CSEvent<Unit> = event().also { event ->
    first.onChange(event::fire).also { parent?.register(it) }
    second.onChange(event::fire).also { parent?.register(it) }
}

fun Triple<CSHasChange<*>, CSHasChange<*>, CSHasChange<*>>.onChange(
    onChange: () -> Unit
): CSRegistration = CSRegistration(
    first.onChange(onChange), second.onChange(onChange),
    third.onChange(onChange)
)

fun Triple<CSHasChange<*>, CSHasChange<*>, CSHasChange<*>>.action(
    onChange: () -> Unit
): CSRegistration {
    onChange()
    return onChange(onChange)
}

fun CSQuadruple<CSHasChange<*>, CSHasChange<*>, CSHasChange<*>, CSHasChange<*>>.onChange(
    onChange: () -> Unit
): CSRegistration = CSRegistration(
    first.onChange(onChange), second.onChange(onChange),
    third.onChange(onChange), fourth.onChange(onChange)
)

inline fun <Return> CSHasChange<out Any>.delegate(
    parent: CSHasRegistrations? = null,
    crossinline from: () -> Return,
): CSHasChangeValue<Return> = let { property ->
    object : CSHasChangeValue<Return> {
        override val value: Return get() = from()
        override fun onChange(function: (Return) -> Unit): CSRegistration {
            val value = CSValueFunction(this, value, function)
            return property.onChange {
                if (parent?.registrations.isActive) value(from())
            }.registerTo(parent)
        }
    }
}

@JvmName("delegateChild")
fun <ChildValue> CSHasChange<out Any>.delegate(
    parent: CSHasRegistrations? = null,
    child: () -> CSHasChangeValue<ChildValue>,
): CSHasChangeValue<ChildValue> = let { property ->
    object : CSHasChangeValue<ChildValue> {
        override val value: ChildValue get() = child().value
        override fun onChange(function: (ChildValue) -> Unit): CSRegistration {
            val value = CSValueFunction(this, value, function)
            var registration: CSRegistration? = null
            var childRegistration: CSRegistration? = null
            val parentRegistration = property.action {
                childRegistration?.cancel()
                val childItem = child()
                // TODO: This works because "isActive = this?.isActive != false" but its super weird
                //  this returns isActive if registration is null basically what is nonsense but fort his case it is what we want...
                if (parent?.registrations.isActive && registration.isActive) childItem.also {
                    value(it.value)
                }
                childRegistration = childItem.onChange {
                    if (parent?.registrations.isActive && registration.isActive) value(it)
                }
            }
            return CSRegistration(isActive = true, onCancel = {
                parentRegistration.cancel()
                childRegistration?.cancel()
            }).also { registration = it }.registerTo(parent)
        }
    }
}

