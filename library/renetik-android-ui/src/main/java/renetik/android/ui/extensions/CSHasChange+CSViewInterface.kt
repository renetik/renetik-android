package renetik.android.ui.extensions

import renetik.android.core.lang.Func
import renetik.android.event.common.CSLaterOnceFunc.Companion.laterOnceFunc
import renetik.android.event.registration.CSHasChange
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationsMap
import renetik.android.ui.protocol.CSViewInterface


inline fun <Argument> CSHasChange<Argument>.onChangeAfterLayout(
    parent: CSViewInterface,
    crossinline function: Func
): CSRegistration {
    val registrations = CSRegistrationsMap(this)
    val laterOnceFunction = registrations.laterOnceFunc({ function() })
    registrations.register(onChange {
        registrations.register(parent.registerAfterLayout {
            laterOnceFunction()
        })
    })
    return registrations
}