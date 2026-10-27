package renetik.android.preset.property

import renetik.android.event.property.CSProperty
import renetik.android.store.CSStore
import renetik.android.store.property.CSStoreProperty

interface CSPresetProperty<T> : CSStoreProperty<T>, CSPresetKeyData {
    val default: T
    override val store get() = preset.store
    val isFollowPreset: CSProperty<Boolean>
    override fun clearKeyData() = super.clear()
    override fun clear() = super.clear()
}

/** The value [store] holds for this property, or its default when [store] holds none. */
fun <T> CSPresetProperty<T>.valueIn(store: CSStore): T? = if (store.has(key)) get(store) else default

fun <T : CSPresetProperty<*>> T.isModifiedIn(store: CSStore) = value != valueIn(store)

