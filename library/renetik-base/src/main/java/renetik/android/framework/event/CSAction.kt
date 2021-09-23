package renetik.android.framework.event

import renetik.android.framework.event.property.CSEventProperty
import renetik.android.framework.event.property.CSEventPropertyFunctions.property
import renetik.android.framework.lang.isTrue
import renetik.android.framework.lang.setFalse
import renetik.android.framework.lang.setTrue
import renetik.android.primitives.ifTrue

interface CSActionInterface : CSEventProperty<Boolean> {
    val isObserved: Boolean
    fun onObserveChange(function: (CSActionInterface) -> Unit): CSEventRegistration
    val isRunning: Boolean

    //    fun onChange(function: (CSActionInterface) -> Unit): CSEventRegistration
    fun start()
    fun stop()
}

fun CSActionInterface.toggle() = apply { if (isRunning) stop() else start() }
val CSActionInterface.isStopped get() = !isRunning
fun CSActionInterface.runIf(condition: Boolean) = condition.ifTrue { start() } ?: stop()

//TODO: Refactor to something simple please
class CSAction(val id: String) : CSActionInterface {

    companion object {
        fun action(id: String): CSActionInterface = CSAction(id)
    }

    private val property = property(id, default = false)
    private val eventIsObserved = event()
    private var observerCount = 0

    override val isObserved get() = observerCount > 0

    override fun onObserveChange(function: (CSActionInterface) -> Unit) =
        eventIsObserved.listen { function(this) }

    override val isRunning get() = property.isTrue

    override fun onChange(function: (Boolean) -> Unit): CSEventRegistration {
        observerCount++
        if (observerCount == 1) eventIsObserved.fire()
        return CSActionOnChangeEventRegistration(property.onChange { function(it) })
    }

    override fun value(newValue: Boolean, fire: Boolean) = property.value(newValue, fire)
    override fun apply() = property.apply()
    override var value: Boolean
        get() = property.value
        set(value) {property.value = value}

    override fun start() {
        property.setTrue()
    }

    override fun stop() {
        property.setFalse()
    }

    override fun onBeforeChange(value: (Boolean) -> Unit) = property.onBeforeChange(value)

    inner class CSActionOnChangeEventRegistration(
        private val registration: CSEventRegistration) : CSEventRegistration {
        override var isActive
            get() = registration.isActive
            set(value) {
                registration.isActive = value
            }

        override fun cancel() {
            registration.cancel()
            observerCount--
            if (observerCount == 0) eventIsObserved.fire()
        }
    }
}