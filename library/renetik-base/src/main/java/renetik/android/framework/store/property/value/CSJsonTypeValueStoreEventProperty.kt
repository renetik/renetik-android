package renetik.android.framework.store.property.value

import renetik.android.framework.event.CSRegistration
import renetik.android.framework.event.listen
import renetik.android.framework.json.data.CSJsonObject
import renetik.android.framework.store.CSStoreInterface
import renetik.kotlin.reflect.createInstance
import kotlin.reflect.KClass

class CSJsonTypeValueStoreEventProperty<T : CSJsonObject>(
    store: CSStoreInterface, key: String, val type: KClass<T>,
    listenStoreChanged: Boolean = false,
    onApply: ((value: T) -> Unit)? = null
) : CSValueStoreEventProperty<T>(store, key, listenStoreChanged, onApply) {

    override val defaultValue get() = type.createInstance()!!

    override var _value = load()
        set(value) {
            field = value
            updateOnChanged()
        }

    init {
        updateOnChanged()
    }

    var valueEventChanged: CSRegistration? = null

    private fun updateOnChanged() {
        valueEventChanged?.cancel()
        valueEventChanged = _value.eventChanged.listen { save() }
    }

    override fun get(store: CSStoreInterface) =
        store.getJsonObject(key, type)

    override fun set(store: CSStoreInterface, value: T) =
        store.set(key, value)
}