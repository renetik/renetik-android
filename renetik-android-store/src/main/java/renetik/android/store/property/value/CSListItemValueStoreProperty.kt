package renetik.android.store.property.value

import renetik.android.core.kotlin.toId
import renetik.android.core.lang.variable.CSListValuesVariable
import renetik.android.json.obj.getValue
import renetik.android.store.CSStore

class CSListItemValueStoreProperty<T>(
    store: CSStore, key: String,
    val getValues: () -> Collection<T>,
    val getDefault: () -> T,
    onChange: ((value: T) -> Unit)? = null
) : CSValueStoreProperty<T>(store, key,onChange), CSListValuesVariable<T> {
    override val values: List<T> get() = getValues().toList()
    override val default: T get() = getDefault()
    override fun get(store: CSStore): T? = store.getValue(key, values)
    override fun set(store: CSStore, value: T) = store.set(key, value.toId())

    /** Takes the item in [values] equal to the loaded one when it is a newer instance. */
    fun refreshItem() {
        val current = values.find { it == loadedValue } ?: return
        if (current === loadedValue) return
        loadedValue = current
        onValueChanged(current)
    }
}