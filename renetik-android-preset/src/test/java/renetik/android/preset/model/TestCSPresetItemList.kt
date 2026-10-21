package renetik.android.preset.model

import renetik.android.core.kotlin.className
import renetik.android.core.kotlin.collections.put
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.invoke
import renetik.android.preset.CSPresetItem
import renetik.android.preset.CSPresetItemList

class TestCSPresetItemList(firstItemId: String) : CSPresetItemList<CSPresetItem> {
    override val id = className
    override val eventReload: CSEvent<Unit> = event()
    override val eventRemove = event<CSPresetItem>()
    override val categories: List<String> = listOf(defaultCategory, userCategory)
    private val defaultPresetItems = mutableListOf<CSPresetItem>()
    private val userPresetItems = mutableListOf<CSPresetItem>()

    init {
        createItem(firstItemId, category = defaultCategory)
    }

    override fun remove(item: CSPresetItem) {
        defaultPresetItems.remove(item)
        userPresetItems.remove(item)
        eventRemove(item)
    }

    override fun items(category: String): List<CSPresetItem> = when (category) {
        defaultCategory -> defaultPresetItems
        userCategory -> userPresetItems
        else -> emptyList()
    }

    override val items: List<CSPresetItem> get() = defaultPresetItems + userPresetItems

    override fun createItem(title: String, category: String): CSPresetItem =
        mutableItems(category).put(CSPresetTestItem(id = title))

    private fun mutableItems(category: String) = when (category) {
        defaultCategory -> defaultPresetItems
        userCategory -> userPresetItems
        else -> error("Unsupported preset category: $category")
    }

    override fun reload() = eventReload()
}
