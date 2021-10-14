package renetik.android.framework.preset.property.value

import renetik.android.framework.preset.CSPreset
import renetik.android.framework.store.CSStoreInterface

class CSBooleanValuePresetEventProperty(
    preset: CSPreset<*, *>,
    key: String,
    override val default: Boolean,
    onChange: ((value: Boolean) -> Unit)?)
    : CSValuePresetEventProperty<Boolean>(preset, key, onChange) {
    override var _value = load()
    override fun get(store: CSStoreInterface) = store.getBoolean(key)
    override fun set(store: CSStoreInterface, value: Boolean) = store.set(key, value)
}