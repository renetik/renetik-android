package renetik.android.event.change

import kotlinx.coroutines.Dispatchers.Main
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.Fun
import renetik.android.core.lang.SusFun
import renetik.android.event.dispatch.CSDebouncer.Companion.debouncer
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrations
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.event.registration.plus
import kotlin.coroutines.CoroutineContext

inline fun <T : CSHasChange<*>> List<T>.onChange(
    crossinline function: Fun
): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    forEach {
        registrations.register(it.onChange {
            if (registrations.isActive) function()
        })
    }
    return registrations
}

inline fun <T : CSHasChange<*>> List<T>.action(
    crossinline function: Fun
): CSRegistration {
    function()
    return onChange(function)
}

inline fun <T : CSHasChange<*>> List<T>.onChangeLaterOnce(
    dispatcher: CoroutineContext = Main, crossinline function: SusFun
): CSRegistrations {
    val registrations = CSRegistrationsMap(debugClassName)
    val laterOnceFunction = registrations.debouncer(dispatcher) {
        if (registrations.isActive) function()
    }
    forEach { registrations + it.onChange { laterOnceFunction() } }
    return registrations
}

inline fun <T : CSHasChange<*>> List<T>.onChangeLaterOnce(
    crossinline function: SusFun
) = onChangeLaterOnce(Main, function)

inline fun <T : CSHasChange<*>> List<T>.actionLaterOnce(
    isActionNow: Boolean = false,
    crossinline function: () -> Unit
): CSRegistrations {
    val registrations = CSRegistrationsMap(debugClassName)
    val laterOnceFunction = registrations.debouncer {
        if (registrations.isActive) function()
    }
    forEach { registrations + it.onChange { laterOnceFunction() } }
    if (isActionNow) function() else laterOnceFunction()
    return registrations
}

inline fun <T : CSHasChange<*>> List<T>.actionLaterOnce(
    crossinline function: () -> Unit
) = actionLaterOnce(isActionNow = false, function)