package renetik.android.preset

import renetik.android.core.kotlin.unexpected
import renetik.android.core.lang.value.isTrue
import renetik.android.core.lang.variable.setFalse
import renetik.android.core.lang.variable.setTrue
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.CSSuspendEvent.Companion.suspendEvent
import renetik.android.event.change.CSHasChange
import renetik.android.event.change.delegate
import renetik.android.event.change.invoke
import renetik.android.event.invoke
import renetik.android.event.lifecycle.CSHasDestruct
import renetik.android.event.lifecycle.CSModel
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.event.registration.plus
import renetik.android.preset.property.CSPresetKeyData
import renetik.android.store.CSStore
import renetik.android.store.type.CSJsonObjectStore.Companion.CSJsonObjectStore

class CSPreset<PresetListItem : CSPresetItem,
        PresetList : CSPresetItemList<PresetListItem>>(
    parent: CSHasDestruct, val parentStore: CSStore,
    key: String, val list: PresetList,
    notFoundItem: (CSStore) -> PresetListItem, defaultItemId: String? = null,
) : CSModel(parent), CSHasChange<Unit> {

    companion object {
        fun <PresetItem : CSPresetItem,
                PresetList : CSPresetItemList<PresetItem>> CSPreset(
            parent: CSHasDestruct, parentPreset: CSPreset<*, *>,
            key: String, list: PresetList,
            notFoundItem: (CSStore) -> PresetItem, defaultItemId: String? = null,
        ): CSPreset<PresetItem, PresetList> = CSPreset(
            parent, parentPreset.store, key, list, notFoundItem, defaultItemId
        ).also { preset ->
            parentPreset.add(preset)
        }

        const val PresetTitleKey = "preset title"
    }

    val id = "$key preset"
    val isFollowStore = property(true)
    val onReload = suspendEvent<PresetListItem>()
    val isReloadInternal = property(false)
    val eventLoad = event<PresetListItem>()
    val eventLoadData = event<PresetListItem>()
    val eventSave = suspendEvent<PresetListItem>()
    val eventChange = event<PresetListItem>()
    val eventDelete = suspendEvent<PresetListItem>()
    val properties = mutableListOf<CSPresetKeyData>()
    val presets = mutableListOf<CSPreset<*, *>>()

    val store = CSPresetStore(this)
    val listItem = CSPresetListItem(this, notFoundItem, defaultItemId)
    val title = property(this, PresetTitleKey, getDefault = { listItem.value.title.value })
        .trackModified()
    val itemTitle = listItem.delegate(this, fromValueChild = { it.title })

    fun clear() {
        store.clear()
        store.clearKeyData()
        listItem.clearKeyData()
        properties.toList().forEach { it.clear() }
    }

    val isModified: Boolean get() = isModifiedIn(listItem.value.store)

    private fun isModifiedIn(store: CSStore): Boolean =
        properties.any { it.isTrackedModifiedIn(store) } || presets.any {
            store.getMap(it.store.key)?.let { data ->
                it.isModifiedIn(CSJsonObjectStore(data))
            } ?: it.isModifiedIn(it.listItem.value.store)
        }

    fun <T : CSPresetKeyData> add(property: T): T {
        if (property in properties) unexpected()
        properties += property
        property.eventDestruct { properties -= property }
        return property
    }

    private fun add(preset: CSPreset<*, *>) {
        if (preset in presets) unexpected()
        preset + isReloadInternal.onChange { preset.isReloadInternal.value = it }
        add(preset.listItem)
//        add(preset.store)
        presets += preset
        preset.eventDestruct { presets -= preset }
    }

    suspend fun reload() {
        val item = listItem.value
        onReload.fire(item)
        reload(listItem.value)
    }

    // Fron now used outside just in Metronome when ticks change to reset to default preset...
    fun reloadInternal() = reload(listItem.value)

    internal fun reload(item: PresetListItem) {
        val isAlreadyReloading = isReloadInternal.isTrue
        if (!isAlreadyReloading) isReloadInternal.setTrue()
        eventLoad(item)
        reload(item.store.data)
        eventChange(item)
        if (!isAlreadyReloading) isReloadInternal.setFalse()
    }

    fun reload(data: Map<String, Any?>) {
        eventLoadData(listItem.value)
        store.reload(data)
        store.save()
        properties.toList().forEach { if (!it.isDestructed) it.onStoreLoaded() }
        presets.toList().forEach { if (!it.isDestructed) it.store.onStoreLoaded() }
    }

    suspend fun saveAsCurrent() = save(listItem.value)

    suspend fun save(item: PresetListItem) = eventSave.fire(item)

    suspend fun delete(item: PresetListItem) {
        list.remove(item)
        eventDelete.fire(item)
    }

    override fun toString() = "$id ${super.toString()}"

    fun onBeforeChange(function: () -> Unit) = eventLoad.listen { function() }

    override fun onChange(function: (Unit) -> Unit) =
        eventChange.listen { function(Unit) }

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    var onSaveToParentPresetItemStore: (String, CSStore) -> Unit =
        { _, itemStore -> this.store.saveTo(itemStore) }
}
