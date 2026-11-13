package renetik.android.event.android

import android.content.Context
import android.content.ContextWrapper
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.lang.CSEnvironment.app
import renetik.android.core.lang.CSLeakCanary.expectWeaklyReachable
import renetik.android.core.logging.CSLog.logErrorTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.parent
import renetik.android.event.registration.CSRegistrationsMap

abstract class CSContext : ContextWrapper, CSHasContext {
    constructor() : super(app)
    constructor(context: Context) : super(context)

    constructor(parent: CSContext) : this(parent.context) {
        parent(parent)
    }

    constructor(parent: CSHasContext) : this(parent.context) {
        parent(parent)
    }

    constructor(parent: CSHasDestruct, context: Context) : this(context) {
        parent(parent)
    }

    // Returning wrapped context as there is no use of this wrapped in child anywhere....
    final override val context: Context get() = baseContext
    final override val registrations = CSRegistrationsMap(debugClassName)
    final override val eventDestruct = event<Unit>()
    @Volatile final override var isDestructed = false
        private set

    override fun onDestruct() {
        if (isDestructed) {
            logErrorTrace { "Already destroyed: $this" }
            return
        }
        isDestructed = true
        registrations.cancel()
        eventDestruct.fire().clear()
        expectWeaklyReachable { "$debugClassName $this onDestruct" }
    }
}