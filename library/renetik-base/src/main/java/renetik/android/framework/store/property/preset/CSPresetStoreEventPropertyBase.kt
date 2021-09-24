package renetik.android.framework.store.property.preset

import renetik.android.framework.event.property.CSEventPropertyBase
import renetik.android.framework.store.CSStoreInterface

abstract class CSPresetStoreEventPropertyBase<T>(
    override var store: CSStoreInterface,
    override val key: String, onApply: ((value: T) -> Unit)? = null) :
    CSEventPropertyBase<T>(onApply), CSPresetStoreEventProperty<T> {

    protected abstract var _value: T

    override var value: T
        get() = _value
        set(value) {
            value(value)
        }

    override fun reload() {
        val newValue = load(store)
        if (_value == newValue) return
        val before = _value
        _value = newValue
        onApply?.invoke(newValue)
        fireChange(before, newValue)
    }

    override fun value(newValue: T, fire: Boolean) = apply {
        if (_value == newValue) return this
        val before = _value
        _value = newValue
        save(store, value)
        onApply?.invoke(newValue)
        if (fire) fireChange(before, newValue)
    }
}