package renetik.android.controller.view.grid

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import renetik.android.controller.base.CSView
import renetik.android.core.android.content.displayWidth

val <T : CSView<RecyclerView>> T.columnCount: Int
    get() = (view.layoutManager as? GridLayoutManager)?.spanCount ?: 1

fun <T : CSView<RecyclerView>> T.sectionGridLayout(
    columnsCount: Int, nonColumnIds: List<Int>,
) = apply {
    view.layoutManager = GridLayoutManager(this, columnsCount).apply {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = when {
                isDestructed -> 0
                view.adapter?.getItemViewType(position) in nonColumnIds -> columnsCount
                else -> 1
            }
        }
    }
}

fun <T : CSView<RecyclerView>> T.sectionGridLayout(
    columnsCount: Int, nonColumnId: Int
) = sectionGridLayout(columnsCount, listOf(nonColumnId))

fun <T : CSView<RecyclerView>> T.autoFitGridLayout(columnWidth: Int) = apply {
    view.layoutManager = GridLayoutManager(this, displayWidth / columnWidth)
}

fun <T : CSView<RecyclerView>> T.columnLayout(columnsCount: Int) = apply {
    view.layoutManager = GridLayoutManager(this, columnsCount)
}

fun <T : CSView<RecyclerView>> T.linearLayout(isHorizontal: Boolean) = apply {
    view.layoutManager = LinearLayoutManager(
        this, if (isHorizontal) HORIZONTAL else VERTICAL, false)
}
