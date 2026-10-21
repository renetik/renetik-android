package renetik.android.preset

import renetik.android.core.lang.CSHasId
import renetik.android.event.CSEvent

interface CSPresetItemList<PresetItem : CSPresetItem>
    : CSHasId, Collection<PresetItem> {
    val categories: List<String>
    fun items(category: String): List<PresetItem>
    val items: List<PresetItem>
    fun remove(item: PresetItem)

    /** Fires with each removed item, so every preset showing it is left without it. */
    val eventRemove: CSEvent<PresetItem>

    fun createItem(title: String, category: String): PresetItem
    val eventReload: CSEvent<Unit>
    fun reload()

    //Collection
    override val size: Int get() = categories.sumOf { items(it).size }
    override fun isEmpty(): Boolean = categories.all { items(it).isEmpty() }
    override fun contains(element: PresetItem): Boolean =
        categories.any { items(it).contains(element) }

    override fun containsAll(elements: Collection<PresetItem>): Boolean =
        elements.all { contains(it) }

    override fun iterator(): Iterator<PresetItem> =
        categories.flatMap { items(it) }.iterator()

    fun find(predicate: (PresetItem) -> Boolean): PresetItem? {
        categories.forEach { category ->
            items(category).find(predicate)?.let { return it }
        }
        return null
    }

    operator fun get(index: Int): PresetItem {
        var categoryIndex = index
        categories.forEach { category ->
            val items = items(category)
            if (categoryIndex < items.size) return items[categoryIndex]
            categoryIndex -= items.size
        }
        throw IndexOutOfBoundsException("$index")
    }

    fun at(index: Int): PresetItem? = if (index in indices) get(index) else null

    fun index(item: PresetItem): Int? {
        var index = 0
        categories.forEach { category ->
            val items = items(category)
            val itemIndex = items.indexOf(item)
            if (itemIndex >= 0) return index + itemIndex
            index += items.size
        }
        return null
    }
}
