package renetik.android.preset

import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.Fun
import renetik.android.event.change.CSHasChange
import renetik.android.event.dispatch.CSDebouncer.Companion.debouncer
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.preset.Preset

//inline fun <T : CSHasChange<*>> List<T>.onChange(
//    preset: Preset, crossinline function: Func
//): CSRegistration {
//    val registrations = CSRegistrationsList(this)
//    forEach { registrations.register(it.onChange { function() }) }
//    return CSRegistration(
//        registrations,
//        preset.onBeforeChange { registrations.pause() },
//        preset.onChange {
//            function()
//            registrations.resume()
//        }
//    )
//}

inline fun <T : CSHasChange<*>> List<T>.onChangeLater(
    preset: Preset, crossinline function: Fun
): CSRegistration {
    val registrations = CSRegistrationsMap(debugClassName)
    val laterOnceFunction = registrations.debouncer { function() }
    forEach { registrations.register(it.onChange { laterOnceFunction() }) }
    return CSRegistration(
        registrations,
        preset.onBeforeChange { registrations.pause() },
        preset.onChange {
            function()
            registrations.resume()
        }
    )
}