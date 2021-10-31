package renetik.android.framework.store.property.value

import renetik.android.framework.event.property.CSEventPropertyBase
import renetik.android.framework.store.CSStoreInterface
import renetik.android.framework.store.property.CSStoreEventProperty

abstract class CSValueStoreEventProperty<T>(
    final override val store: CSStoreInterface,
    final override val key: String,
    onChange: ((value: T) -> Unit)? = null)
    : CSEventPropertyBase<T>(onChange), CSStoreEventProperty<T> {

    abstract val defaultValue: T
    protected abstract var _value: T
    abstract fun get(store: CSStoreInterface): T?

    fun load(store: CSStoreInterface): T = get(store) ?: defaultValue
    fun load(): T = load(store)

//    private val storeEventChangedRegistration = store.eventChanged.listen {
//        _value = load()
//    }

    final override var value: T
        get() = _value
        set(value) = value(value)


    override fun value(newValue: T, fire: Boolean) {
        if (_value == newValue) return
        _value = newValue
        //        storeEventChangedRegistration.pause().use { set(store, newValue) }
        set(store, newValue)
        onApply?.invoke(newValue)
        if (fire) eventChange.fire(newValue)
    }

    override fun toString() = "$key $value"
}