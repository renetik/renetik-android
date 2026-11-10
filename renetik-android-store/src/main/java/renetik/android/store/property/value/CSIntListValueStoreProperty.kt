package renetik.android.store.property.value

import renetik.android.store.CSStore

class CSIntListValueStoreProperty(
    store: CSStore, key: String,
    private val getDefault: () -> List<Int>,
    onChange: ((value: List<Int>) -> Unit)? = null
) : CSValueStoreProperty<List<Int>>(store, key, onChange) {
    constructor(
        store: CSStore, key: String, default: List<Int>,
        onChange: ((value: List<Int>) -> Unit)? = null
    ) : this(store, key, { default }, onChange)

    override val default: List<Int> get() = getDefault()

    override fun get(store: CSStore) = store.getString(key)?.split(",")
        ?.mapNotNull { it.toIntOrNull() } ?: default

    override fun set(store: CSStore, value: List<Int>) =
        store.set(key, value.joinToString(","))
}