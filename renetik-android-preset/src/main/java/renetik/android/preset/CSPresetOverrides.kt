package renetik.android.preset

/**
 * Lets a parent store only what a preset changes from its item, so the rest follows the item. On
 * load the stored data lies over the item's data, unless the parent's item stores this preset
 * whole. [changes] holds the keys the parent stored and those changed since that load, with null
 * for a key the preset removed from the item's data.
 */
class CSPresetOverrides(private val preset: CSPreset<*, *>) {
    var isEnabled = false
    var isStoredWholeBy: (CSPresetItem) -> Boolean = { false }
    var parent: CSPreset<*, *>? = null
    private var loadingKeys = emptySet<String>()
    private var storedKeys = emptySet<String>()
    private var loadedData = emptyMap<String, Any?>()

    private val itemData: Map<String, Any?>
        get() = if (preset.listItem.isNotFound) emptyMap() else preset.listItem.value.store.data

    private val isLoadedOverItem: Boolean
        get() = isEnabled && parent?.listItem?.value?.let(isStoredWholeBy) != true

    private val parentItemStored: Map<String, Any?>
        get() = parent?.listItem?.value?.store?.getMap(preset.store.key).orEmpty()

    fun fullData(stored: Map<String, Any?>): Map<String, Any?> =
        if (isLoadedOverItem) itemData + stored else stored

    /**
     * Takes up the data a preset created after its parent's load found there. Data as the parent's
     * item stores it still has to load over the item. Anything else is the state of an earlier
     * run, whose keys neither item holds count as loaded.
     */
    fun onCreatedWithData() {
        val stored = parentItemStored
        if (preset.store.data == stored) preset.reloadStored(stored)
        else {
            storedKeys = stored.keys
            loadedData = preset.store.data + fullData(stored)
        }
    }

    fun onLoadStored(stored: Map<String, Any?>) {
        loadingKeys = stored.keys
    }

    fun onLoaded() {
        if (!isEnabled) return
        storedKeys = loadingKeys
        loadingKeys = emptySet()
        loadedData = preset.store.data.toMap()
    }

    val changes: Map<String, Any?>
        get() {
            val data = preset.store.data
            val itemData = itemData
            val changed = data.filter { (key, value) ->
                (key in storedKeys || value != loadedData[key]) && value != itemData[key]
            }
            return changed + (itemData.keys - data.keys).associateWith { null }
        }
}
