package renetik.android.store.property.value

import renetik.android.event.lifecycle.onDestructed
import renetik.android.event.registration.CSRegistration
import renetik.android.event.registration.cancelRegistrations
import renetik.android.store.CSStore
import renetik.android.store.type.CSJsonObjectStore
import kotlin.reflect.KClass

class CSJsonListValueStoreProperty<T : CSJsonObjectStore>(
    store: CSStore, key: String, val type: KClass<T>,
    onApply: ((value: List<T>) -> Unit)? = null
) : CSValueStoreProperty<List<T>>(store, key, onApply) {
    override val default: List<T> = emptyList()
    override fun get(store: CSStore): List<T> = store.getJsonObjectList(key, type) ?: this.default
    override fun set(store: CSStore, value: List<T>) = store.setJsonObjectList(key, value)

    private val valuesOnChanged = mutableListOf<CSRegistration>()
        .also { onDestructed(it::cancelRegistrations) }

    override fun onLoadedValueChanged(value: List<T>?) {
        valuesOnChanged.cancelRegistrations()
        value?.forEach { valuesOnChanged.add(it.eventChanged.onChange { set(store, value) }) }
    }
}
