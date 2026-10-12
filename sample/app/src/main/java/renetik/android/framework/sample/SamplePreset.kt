package renetik.android.framework.sample

import renetik.android.core.kotlin.collections.put
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.invoke
import renetik.android.preset.CSPreset
import renetik.android.preset.CSPresetItem
import renetik.android.preset.CSPresetItemList
import renetik.android.preset.property.CSPresetKeyData
import renetik.android.store.operation
import renetik.android.store.property
import renetik.android.store.property.CSStoreProperty
import renetik.android.store.type.CSJsonObjectStore

const val sampleCategory = "default"

class SamplePresetItem(override val id: String) : CSPresetItem {
    override val store = CSJsonObjectStore()
    override val title: CSStoreProperty<String> = store.property("title", id)
}

class SamplePresetItemList : CSPresetItemList<SamplePresetItem> {
    override val id = "SamplePresetItemList"
    override val eventReload: CSEvent<Unit> = event()
    override val categories: List<String> = listOf(sampleCategory)
    override val items: List<SamplePresetItem>
        field = mutableListOf(SamplePresetItem("default"))
    override fun items(category: String): List<SamplePresetItem> = items
    override fun createItem(title: String, category: String): SamplePresetItem =
        items.put(SamplePresetItem(title))

    override fun remove(item: SamplePresetItem) {
        items.remove(item)
    }

    override fun reload() = eventReload()
}

/** Persist preset properties into the selected item store on save. */
fun <T : CSPreset<*, *>> T.manageItems() = apply {
    eventSave.listen { item ->
        item.store.operation {
            it.clear()
            for (property: CSPresetKeyData in properties) property.saveTo(it)
            for (preset: CSPreset<*, *> in presets) preset.store.saveTo(it)
        }
    }
}
