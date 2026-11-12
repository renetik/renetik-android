package renetik.android.controller.view.grid.item

import renetik.android.controller.view.grid.GridViewOut
import renetik.android.controller.view.grid.item.CSSectionItem.Companion.ItemSectionEmptyViewId
import renetik.android.controller.view.grid.item.CSSectionItem.Companion.ItemSectionHeaderViewId
import renetik.android.controller.view.grid.item.CSSectionItem.Companion.ItemSectionViewId
import renetik.android.controller.view.grid.item.CSSectionItem.Companion.ItemViewId
import renetik.android.core.lang.value.isFalse

fun <Item> GridViewOut<CSSectionItem<CSItemSection<Item>, Item>>.reload(
    sections: List<CSItemSection<Item>>,
    filter: ((items: Collection<Item>) -> Collection<Item>)? = null
) {
    val items = mutableListOf<Pair<CSSectionItem<CSItemSection<Item>, Item>, Int>>()
    sections.forEachIndexed { index, section -> items.load(section, index, filter) }
    reload(items)
}

fun <Item> MutableList<Pair<CSSectionItem<CSItemSection<Item>, Item>, Int>>.load(
    section: CSItemSection<Item>, index: Int,
    filter: ((items: Collection<Item>) -> Collection<Item>)? = null
) {
    section.header?.also {
        this += CSSectionItem<CSItemSection<Item>, Item>(section, index) to ItemSectionHeaderViewId
    }
    this += CSSectionItem<CSItemSection<Item>, Item>(section, index) to ItemSectionViewId
    if (section.isCollapsed.isFalse) loadItems(section, index, filter)
}

fun <Item> MutableList<Pair<CSSectionItem<CSItemSection<Item>, Item>, Int>>.loadItems(
    section: CSItemSection<Item>, index: Int,
    filter: ((items: Collection<Item>) -> Collection<Item>)? = null
) {
    val visibleItems = filter?.invoke(section.items) ?: section.items
    if (visibleItems.isEmpty())
        this += CSSectionItem<CSItemSection<Item>, Item>(
            section, index) to ItemSectionEmptyViewId
    else visibleItems.forEachIndexed { itemIndex, item ->
        this += CSSectionItem(section, itemIndex, item) to ItemViewId
    }
}

