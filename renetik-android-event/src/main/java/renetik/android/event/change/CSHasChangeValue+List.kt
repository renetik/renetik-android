@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.event.change

import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.ArgFun
import renetik.android.event.registration.CSHasRegistrations
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistration.Companion.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.event.registration.isActive
import renetik.android.event.registration.plus
import renetik.android.event.registration.registerTo

inline fun <T : CSHasChangeValue<Value>, Value> List<T>.onChange(
    crossinline function: ArgFun<List<Value>>
): CSRegistration = CSRegistrationsMap(debugClassName).also { registrations ->
    forEach { item: CSHasChangeValue<Value> ->
        registrations + item.onChange {
            if (registrations.isActive) function(map { it.value })
        }
    }
}

inline fun <T : CSHasChangeValue<Value>, Value> List<T>.action(
    crossinline function: ArgFun<List<Value>>
): CSRegistration {
    function(map { it.value })
    return onChange(function)
}

@JvmName("delegateChild")
fun <Argument, Return> List<CSHasChangeValue<Argument>>.delegate(
    parent: CSHasRegistrations? = null,
    child: (List<Argument>) -> CSHasChangeValue<Return>,
): CSHasChangeValue<Return> = let { properties ->
    object : CSHasChangeValue<Return> {
        override val value: Return get() = child(properties.map { it.value }).value
        override fun onChange(function: (Return) -> Unit): CSRegistration {
            val value = CSValueFunction(this, value, function)
            var registration: CSRegistration? = null
            var childRegistration: CSRegistration? = null
            val parentRegistration = properties.action { parentValue ->
                childRegistration?.cancel()
                val childItem = child(parentValue)
                if (parent?.registrations.isActive && registration.isActive)
                    childItem.also { value(it.value) }
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
