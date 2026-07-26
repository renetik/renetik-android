package renetik.android.store.property.nullable

import renetik.android.store.CSStore
import renetik.android.store.property.value.CSValueStoreProperty

class CSIntNullableStoreProperty(
    store: CSStore, key: String,
    override val default: Int? = null,
    onChange: ((value: Int?) -> Unit)? = null)
    : CSValueStoreProperty<Int?>(store, key, onChange) {
    override fun get(store: CSStore): Int? = store.getInt(key)
    override fun set(store: CSStore, value: Int?) =
        value?.let { store.set(key, value) } ?: store.clear(key)
}


// TODO move to separate file
class CSDoubleNullableStoreProperty(
    store: CSStore, key: String,
    override val default: Double? = null,
    onChange: ((value: Double?) -> Unit)? = null)
    : CSValueStoreProperty<Double?>(store, key, onChange) {
    override fun get(store: CSStore): Double? = store.getDouble(key)
    override fun set(store: CSStore, value: Double?) =
        value?.let { store.set(key, value) } ?: store.clear(key)
}