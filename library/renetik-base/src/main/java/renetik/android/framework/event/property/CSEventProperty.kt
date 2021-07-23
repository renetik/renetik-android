package renetik.android.framework.event.property

import renetik.android.framework.event.event
import renetik.android.framework.event.listen

open class CSEventProperty<T>(value: T, onChange: ((value: T) -> Unit)? = null) :
    CSEventPropertyInterface<T> {

    private val eventBeforeChange = event<T>()
    private val eventChange = event<T>()
    private var _value: T = value

    init {
        onChange?.let { eventChange.listen(onChange) }
    }

    override var value: T
        get() = _value
        set(value) = value(value)

    override fun value(newValue: T, fire: Boolean) {
        if (_value == newValue) return
        if (fire) eventBeforeChange.fire(_value)
        _value = newValue
        if (fire) eventChange.fire(newValue)
    }

    override fun onBeforeChange(value: (T) -> Unit) = eventBeforeChange.listen(value)
    override fun onChange(value: (T) -> Unit) = eventChange.listen(value)
    fun apply() = apply { eventChange.fire(value) }
    override fun toString() = value.toString()
}