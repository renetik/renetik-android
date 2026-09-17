package renetik.android.store.property.nullable

import renetik.android.store.CSStore
import renetik.android.store.property.value.CSValueStoreProperty

class CSLongNullableStoreProperty(
    store: CSStore, key: String,
    override val default: Long? = null,
    onChange: ((value: Long?) -> Unit)? = null)
    : CSValueStoreProperty<Long?>(store, key, onChange) {
    override fun get(store: CSStore): Long? = store.getLong(key)
    override fun set(store: CSStore, value: Long?) =
        value?.let { store.set(key, value) } ?: store.clear(key)
}
