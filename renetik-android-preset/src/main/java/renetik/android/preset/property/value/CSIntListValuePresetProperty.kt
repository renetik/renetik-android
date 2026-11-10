package renetik.android.preset.property.value

import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.preset.CSPreset
import renetik.android.store.CSStore

class CSIntListValuePresetProperty(
    parent: CSHasDestruct,
    preset: CSPreset<*, *>, key: String,
    private val getDefault: () -> List<Int>,
    onChange: ((value: List<Int>) -> Unit)? = null
) : CSValuePresetProperty<List<Int>>(parent, preset, key, onChange),
    CSHasOnLoaded<List<Int>> {

    constructor(
        parent: CSHasDestruct, preset: CSPreset<*, *>, key: String,
        default: List<Int>, onChange: ((value: List<Int>) -> Unit)? = null
    ) : this(parent, preset, key, { default }, onChange)

    override val default: List<Int> get() = getDefault()

    override var onValueLoaded: ((List<Int>) -> List<Int>)? = null

    override fun get(store: CSStore): List<Int>? {
        val loaded = store.getString(key)?.split(",")?.mapNotNull { it.toIntOrNull() }
        return loaded?.let { onValueLoaded?.invoke(it) ?: it }
    }

    override fun set(store: CSStore, value: List<Int>) =
        store.set(key, value.joinToString(","))
}