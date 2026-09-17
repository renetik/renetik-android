package renetik.android.preset.property.nullable

import renetik.android.event.lifecycle.CSHasRegistrationsHasDestruct
import renetik.android.preset.CSPreset
import renetik.android.store.CSStore

class CSLongNullablePresetProperty(
    parent: CSHasRegistrationsHasDestruct,
    preset: CSPreset<*, *>,
    key: String,
    override val default: Long?,
    onChange: ((value: Long?) -> Unit)?
) : CSNullablePresetProperty<Long>(parent, preset, key, onChange) {
    override fun get(store: CSStore): Long? = store.getLong(key)
    override fun set(store: CSStore, value: Long?) = store.set(key, value)
}
