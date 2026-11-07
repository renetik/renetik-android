package renetik.android.preset

import renetik.android.core.lang.value.isFalse
import renetik.android.event.lifecycle.destruct
import renetik.android.preset.property.CSPresetKeyData
import renetik.android.store.CSStore
import renetik.android.store.type.CSJsonObjectStore

class CSPresetStore(
    override val preset: CSPreset<*, *>,
) : CSJsonObjectStore(), CSPresetKeyData {
    override val key = "${preset.id} store"

    override val isDestructed: Boolean get() = preset.isDestructed
    override val eventDestruct get() = preset.eventDestruct
    override fun onDestruct() = preset.destruct()

    private val parentStore: CSStore = preset.parentStore
    override fun clearKeyData() = parentStore.clear(key)

    override fun saveTo(store: CSStore) = store.set(key, data)
    fun save() = saveTo(parentStore)
//    val isNotStored get() = parentStore.getMap(key).isNullOrEmpty()

    init {
        parentStore.getMap(key)?.let(::load)
// This caused track to save sequences on preset change, not user why it was here at all
// preset.onDestructed(::save)
    }

    override fun onStoreLoaded() {
        if (preset.isFollowStore.isFalse) save()
        else {
            val data = parentStore.getMap(key)
//            if (this.data == data) return
            if (data.isNullOrEmpty()) preset.reloadInternal()
            else preset.reloadStored(data)
        }
    }

    override fun onChanged() {
        super.onChanged()
        if (preset.isReloadInternal.isFalse) save()
    }

    override fun toString() = "key:$key this:${super.toString()}"
    override fun hashCode() = 31 * key.hashCode() + super.hashCode()
    override fun equals(other: Any?) = (other as? CSPresetStore)?.let {
        it.key == key && super.equals(other)
    } ?: super.equals(other)
}