package renetik.android.event.lifecycle

import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.CSLeakCanary.expectWeaklyReachable
import renetik.android.core.logging.CSLog.logWarnTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.registration.CSRegistrationsMap

open class CSModel(
    parent: CSHasDestruct? = null
) : CSHasRegistrationsHasDestruct {
    final override val registrations = CSRegistrationsMap(debugClassName)
    final override val eventDestruct = event<Unit>()
    @Volatile final override var isDestructed = false
        private set

    init {
        parent?.let(::parent)
    }

    override fun onDestruct() {
        if (isDestructed) {
            logWarnTrace { "Already destroyed: $this" }
            return
        }
        isDestructed = true
        registrations.cancel()
        eventDestruct.fire().clear()
        expectWeaklyReachable { "$debugClassName $this onDestruct" }
    }
}

