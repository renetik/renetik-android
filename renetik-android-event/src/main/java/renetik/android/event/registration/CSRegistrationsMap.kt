package renetik.android.event.registration

import androidx.annotation.AnyThread
import renetik.android.core.kotlin.debugClassName
import renetik.android.core.kotlin.collections.removeValue
import renetik.android.core.kotlin.primitives.isTrue
import renetik.android.core.lang.CSLeakCanary.expectWeaklyReachable
import renetik.android.core.lang.Fun
import renetik.android.core.lang.variable.CSVariable.Companion.variable
import renetik.android.core.logging.CSLog.logWarnTrace
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.fire
import renetik.android.event.listen
import java.lang.System.nanoTime
import java.util.concurrent.atomic.AtomicInteger

class CSRegistrationsMap(val id: String) : CSRegistrations, CSHasRegistrations {
    override val registrations: CSRegistrationsMap = this

    @get:Synchronized
    override var isActive by variable(true, ::onActiveChange)
        private set

    @get:Synchronized
    override var isCanceled: Boolean = false
        private set

    override val eventCancel = event()

    private var isCancelling: Boolean = false

    val map: MutableMap<String, CSRegistration> = mutableMapOf()
    private val counter = AtomicInteger(0)
    private fun createUniqueId() = "${counter.incrementAndGet()}-$id-${nanoTime()}"

    private fun onActiveChange(isActive: Boolean) {
        if (isCanceled) {
            logWarnTrace { "Already canceled id:$id" }
            return
        }
        map.forEach { it.value.setActive(isActive) }
    }

    @Synchronized
    @AnyThread
    override fun resume() {
        if (isCanceled) {
            logWarnTrace { "Already canceled:$this" }
            return
        }
        if (!isActive) isActive = true
    }

    @Synchronized
    @AnyThread
    override fun pause() {
        if (isCanceled) {
            logWarnTrace { "Already canceled:$this" }
            return
        }
        if (isActive) isActive = false
    }

    @Synchronized
    @AnyThread
    override fun register(key: String, registration: CSRegistration?): CSRegistration? {
        if (isCanceled) logWarnTrace { "Already canceled:$this" }
        remove(key)
        return registration?.let { add(key, it) }
    }

    @Synchronized
    @AnyThread
    override fun register(
        replace: CSRegistration?,
        registration: CSRegistration?,
    ): CSRegistration? {
        if (isCanceled) logWarnTrace { "Already canceled:$this" }
        replace?.cancel()
        return registration?.let { add(createUniqueId(), it) }
    }

    fun isActive(key: String): Boolean = map[key]?.isActive.isTrue

    @Synchronized
    @AnyThread
    override fun register(registration: CSRegistration): CSRegistration =
        add(createUniqueId(), registration)

    @Synchronized
    @AnyThread
    private fun add(key: String, registration: CSRegistration): CSRegistration {
        if (isCanceled) logWarnTrace { "Already canceled:$this" }
        if (registration.isCanceled) return registration
        if (isCanceled) return registration.also { it.cancel() }
        map[key] = registration
        return object : CSRegistration {
            override val isActive: Boolean get() = registration.isActive
            override val isCanceled: Boolean get() = registration.isCanceled
            override val eventCancel = registration.eventCancel
            override fun resume() = registration.resume()
            override fun pause() = registration.pause()
            override fun cancel() {
                cancel(registration)
                eventCancel.fire()
            }
        }
    }

    @Synchronized
    @AnyThread
    fun cancel(key: String) {
        if (isCanceled)
            logWarnTrace { "Already canceled:$this" }
        remove(key)
    }

    private fun remove(key: String) = map.remove(key)?.cancel()

    @Synchronized
    @AnyThread
    private fun cancel(registration: CSRegistration) {
        if (isCancelling && !isCanceled) {
            registration.cancel()
            return
        }
        val wasPresent = map.removeValue(registration)
        if (registration.isCanceled && !wasPresent) return
        if (!wasPresent) logWarnTrace { "Registration not found:$registration" }
        if (registration.isCanceled) return
        registration.cancel()
        if (isCanceled) logWarnTrace { "Already canceled:$this" }
    }

    val size: Int get() = map.size

    fun clear() = map.cancelRegistrations()

    override fun toString(): String =
        "${super.toString()} id:$id size:$size isActive:$isActive isCanceled:$isCanceled"

    fun onCancel(function: Fun) = eventCancel.listen(function)

    @Synchronized
    @AnyThread
    override fun cancel() {
        if (isCanceled) return
        if (isCancelling) {
            logWarnTrace { "Already cancelling:$this" }
            return
        }
        isCancelling = true
        map.onEach { it.value.cancel() }.clear()
        isCanceled = true
        eventCancel.fire().clear()
        expectWeaklyReachable { "$debugClassName $this cancel" }
    }
}