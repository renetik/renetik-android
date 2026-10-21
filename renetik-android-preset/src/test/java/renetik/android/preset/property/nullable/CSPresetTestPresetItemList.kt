package renetik.android.preset.property.nullable

import renetik.android.core.kotlin.className
import renetik.android.core.kotlin.collections.put
import renetik.android.event.CSEvent
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.invoke
import renetik.android.preset.CSPresetItem
import renetik.android.preset.model.CSPresetTestItem
import renetik.android.preset.model.defaultCategory
import renetik.android.preset.model.userCategory

class CSPresetTestPresetItemList : renetik.android.preset.CSPresetItemList<CSPresetItem> {
    override val id = className
    override val categories: List<String> = listOf(defaultCategory, userCategory)
    private val defaultPresetItems = mutableListOf<CSPresetItem>()
    private val userPresetItems = mutableListOf<CSPresetItem>()
    override val eventReload: CSEvent<Unit> = event()
    override val eventRemove = event<CSPresetItem>()

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

    override fun createItem(title: String, category: String): CSPresetItem {
        val items = mutableItems(category)
        return items.put(CSPresetTestItem(title))
    }

    private fun mutableItems(category: String) = when (category) {
        defaultCategory -> defaultPresetItems
        userCategory -> userPresetItems
        else -> error("Unsupported preset category: $category")
    }

    override fun reload() = eventReload()
}
