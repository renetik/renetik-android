@file:Suppress("NOTHING_TO_INLINE")

package renetik.android.event

import android.annotation.SuppressLint
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import renetik.android.core.kotlin.primitives.isTrue
import renetik.android.core.lang.CSEnvironment.isDebug
import renetik.android.core.kotlin.logError
import renetik.android.core.logging.CSLog.logErrorTrace
import renetik.android.event.dispatch.onMain
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationImpl
import java.util.concurrent.CopyOnWriteArrayList

class CSEventImpl<T> : CSEvent<T> {
    private var onMainParent: CSHasDestruct? = null
    fun onMain(parent: CSHasDestruct) = apply { onMainParent = parent }

    private val listeners = CopyOnWriteArrayList<CSEventListener<T>>()

    @AnyThread
    override fun listen(function: (T) -> Unit): CSRegistration =
        EventListenerImpl(function).also(listeners::add)

    @Volatile private var isPaused = false
    private var isFiring = false

    @AnyThread
    override fun fire(argument: T) {
        if (isPaused) return
        if (!isListened) return
        @SuppressLint("ThreadConstraint")
        onMainParent?.onMain { fireOnMain(argument) }
            ?: run { fireListeners(argument) }
    }

    @AnyThread
    fun fireListeners(argument: T) = listeners.forEach { listener ->
        if (listener.isActive) listener.fire(argument)
    }

    @MainThread
    fun fireOnMain(argument: T) {
        if (isFiring) {
            logErrorTrace { "Event fired while firing" }
            return
        }
        isFiring = true
        fireListeners(argument)
        isFiring = false
    }

    private inline fun CSEventListener<T>.fire(argument: T) {
        if (isDebug) this(argument)
        else runCatching { this(argument) }.logError()
    }

    @AnyThread
    override fun clear() = listeners.clear()

    @get:AnyThread
    override val isListened get() = listeners.isNotEmpty()

    @AnyThread
    override fun pause() {
        isPaused = true
    }

    @AnyThread
    override fun resume() {
        isPaused = false
    }

    override fun onChange(function: (T) -> Unit): CSRegistration = listen(function)

    inner class EventListenerImpl(
        private val listener: (T) -> Unit
    ) : CSEventListener<T>, CSRegistrationImpl(isActive = true) {

        override fun invoke(argument: T) = isActive.isTrue { listener(argument) }

        override fun onCancel() {
            super.onCancel()
            listeners.remove(this)
        }

        override fun toString() = "${super.toString()} listener:${listener::class}"
    }
}
