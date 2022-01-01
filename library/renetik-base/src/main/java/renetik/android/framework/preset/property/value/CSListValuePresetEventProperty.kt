package renetik.android.framework.preset.property.value

import renetik.android.framework.CSEventOwnerHasDestroy
import renetik.android.framework.lang.CSHasId
import renetik.android.framework.preset.CSPreset
import renetik.android.framework.store.CSStoreInterface
import renetik.kotlin.toId

class CSListValuePresetEventProperty<T : CSHasId>(
    parent: CSEventOwnerHasDestroy,
    preset: CSPreset<*, *>, key: String, val values: Iterable<T>,
    override val default: List<T>, onChange: ((value: List<T>) -> Unit)?)
    : CSValuePresetEventProperty<List<T>>(parent, preset, key, onChange) {

    override var _value = load()

    override fun get(store: CSStoreInterface) = store.get(key)?.split(",")
        ?.mapNotNull { categoryId -> values.find { it.id == categoryId } } ?: default

    override fun set(store: CSStoreInterface, value: List<T>) =
        store.set(key, value.joinToString(",") { it.toId() })
}