package renetik.android.event

import renetik.android.core.kotlin.finally
import renetik.android.core.kotlin.logError
import renetik.android.core.kotlin.primitives.isTrue
import renetik.android.core.kotlin.throwCancellation
import renetik.android.core.logging.CSLog.logErrorTrace
import renetik.android.event.change.CSHasChange
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.CSRegistrationImpl
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class CSSuspendEvent<T> : CSHasChange<T> {
    companion object {
        @JvmName("eventWithArgument")
        fun <T> suspendEvent() = CSSuspendEvent<T>()
        fun suspendEvent() = CSSuspendEvent<Unit>()
        suspend operator fun CSSuspendEvent<Unit>.invoke() = fire(Unit)
//
//        operator fun <T> CSSuspendEvent<T>.invoke(
//            function: suspend (T) -> Unit) = listen(function)
//
//        operator fun CSSuspendEvent<Unit>.invoke(
//            function: suspend () -> Unit) = listen { function() }
    }

    private val listeners = CopyOnWriteArrayList<CSSuspendEventListener<T>>()

    @Volatile
    private var paused = false
    private val firing = AtomicBoolean(false)

    fun listen(function: suspend (T) -> Unit): CSRegistration =
        EventListenerImpl(function).also(listeners::add)

    suspend fun fire(argument: T) {
        if (paused) return
        if (!firing.compareAndSet(false, true)) {
            logErrorTrace { "Event fired while firing" }
            return
        }
        runCatching {
            listeners.forEach { listener ->
                if (listener.isActive)
                    runCatching { listener(argument) }
                        .throwCancellation().logError()
            }
        }.finally { firing.set(false) }.getOrThrow()
    }

    fun clear() = listeners.clear()

    val isListened get() = listeners.isNotEmpty()

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    override fun onChange(function: (T) -> Unit): CSRegistration = listen(function)

    fun onChange(function: suspend (T) -> Unit): CSRegistration = listen(function)

    inner class EventListenerImpl(
        private val listener: suspend (T) -> Unit
    ) : CSSuspendEventListener<T>, CSRegistrationImpl(isActive = true) {

        override suspend fun invoke(argument: T) = isActive.isTrue { listener(argument) }

        override fun onCancel() {
            super.onCancel()
            listeners.remove(this)
        }

        override fun toString() = "${super.toString()} listener:${listener::class}"
    }
}
