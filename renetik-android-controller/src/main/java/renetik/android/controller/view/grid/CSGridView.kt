package renetik.android.controller.view.grid

import android.annotation.SuppressLint
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import renetik.android.controller.base.CSActivityViewInterface
import renetik.android.controller.base.CSView
import renetik.android.controller.base.reusable
import renetik.android.controller.view.grid.item.CSGridItemView
import renetik.android.core.kotlin.collections.firstIndex
import renetik.android.core.kotlin.unexpected
import renetik.android.event.CSEvent.Companion.event
import renetik.android.event.change.CSHasChangeValue
import renetik.android.event.change.stateDelegate
import renetik.android.event.dispatch.later
import renetik.android.event.invoke
import renetik.android.event.property.CSProperty
import renetik.android.event.property.CSProperty.Companion.property
import renetik.android.ui.protocol.findView
import renetik.android.ui.view.alphaToDisabled
import renetik.android.ui.view.disabledByAlpha
import renetik.android.ui.view.fadeIn
import renetik.android.ui.view.fadeOut
import renetik.android.ui.view.invisible
import renetik.android.ui.view.shortAnimationDuration
import renetik.android.ui.view.visible
import renetik.android.ui.protocol.onShowUntilHide

typealias GridViewOut<T> = CSGridView<T, out CSGridItemView<T>>
typealias GridView<T> = CSGridView<T, CSGridItemView<T>>

class CSGridView<ItemType : Any,
        ViewType : CSGridItemView<ItemType>>(
    parent: CSActivityViewInterface, viewId: Int,
    val createView: (CSGridView<ItemType, ViewType>,
                     viewType: Int, parent: ViewGroup) -> ViewType
) : CSView<RecyclerView>(parent, viewId) {

    constructor(
        parent: CSActivityViewInterface, viewId: Int,
        createView: (CSGridView<ItemType, ViewType>, parent: ViewGroup) -> ViewType
    ) : this(parent, viewId, { viewParent, _, group -> createView(viewParent, group) })

    constructor(
        parent: CSActivityViewInterface, viewId: Int,
        createView: (CSGridView<ItemType, ViewType>) -> ViewType
    ) : this(parent, viewId, { viewParent, _, _ -> createView(viewParent) })

    val selectedItem: CSProperty<ItemType?> = property(null)
    val data = mutableListOf<Pair<ItemType, Int>>()
    val selectedIndex: CSHasChangeValue<Int?> =
        selectedItem.stateDelegate(fromValue = { item -> data.firstIndex { it.first == item } })

    private val adapter = Adapter()

    fun reload(iterable: Iterable<Pair<ItemType, Int>>) = apply {
        data.clear()
        load(iterable)
    }

    fun load(iterable: Iterable<Pair<ItemType, Int>>) = apply {
        data.addAll(iterable)
        reload()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reload() {
        adapter.notifyDataSetChanged()
        updateEmptyView()
    }

    private val eventItemSelected = event<ViewType>().apply {
        listen { selectedItem.value(it.value) }
    }

    fun onItemSelected(function: (ViewType) -> Unit) =
        apply { eventItemSelected.listen(function) }

    private val eventItemReSelected = event<ViewType>()
    fun onItemReSelected(function: (ViewType) -> Unit) =
        apply { eventItemReSelected.listen(function) }

    private val eventDisabledItemClick = event<ViewType>()
    fun onDisabledItemClick(function: (ViewType) -> Unit) =
        apply { eventDisabledItemClick.listen(function) }

    init {
        view.overScrollMode = OVER_SCROLL_NEVER
        view.adapter = adapter
    }

    private fun ViewType.updateSelection() {
        if (!isLoaded) return
        isSelected = selectedItem.value == value
        contentView.isClickable = !isSelected || eventItemReSelected.isListened
    }

    private fun CSGridItemView<ItemType>.updateDisabled() {
        if (itemDisabled) {
            if (eventDisabledItemClick.isListened) view.alphaToDisabled()
            else view.disabledByAlpha()
        } else {
            view.disabledByAlpha(false)
            view.alpha = 1f
        }
    }

    fun onItemClick(item: ViewType) {
        if (selectedItem.value != item.value) {
            if (item.itemDisabled) eventDisabledItemClick(item)
            else eventItemSelected(item)
        } else eventItemReSelected(item)
    }

    private var emptyView: View? = null
    fun emptyView(id: Int) = apply { emptyView = parentView.findView(id) }
    private fun updateEmptyView() {
        emptyView?.let { if (data.isEmpty()) it.fadeIn() else it.fadeOut() }
    }

    fun scrollToActive(smooth: Boolean = false) = apply {
        data.firstIndex { it.first == selectedItem.value }?.also { position ->
            if (smooth) {
                val scroller = LinearSmoothScroller(this)
                scroller.targetPosition = position
                later(shortAnimationDuration) {
                    view.layoutManager?.startSmoothScroll(scroller) ?: unexpected()
                }
            } else view.scrollToPosition(position)
        }
    }

    private inner class AdapterViewHolder(
        val gridItemView: ViewType
    ) : ViewHolder(gridItemView.view) {
        init {
            gridItemView.reusable()
            gridItemView.view.updateLayoutParams<LayoutParams> { width = MATCH_PARENT }
            gridItemView.onShowUntilHide {
                selectedItem.onChange { gridItemView.updateSelection() }
            }
        }
    }

    override fun onDestruct() {
        view.setAdapter(null)
        view.setLayoutManager(null)
        super.onDestruct()
    }

    private inner class Adapter : RecyclerView.Adapter<AdapterViewHolder>() {

        override fun onCreateViewHolder(group: ViewGroup, type: Int) =
            AdapterViewHolder(createView(this@CSGridView, type, group))

        override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
            holder.gridItemView.let {
                it.view.visible()
                it.load(data[position].first, position)
                it.updateDisabled()
                it.updateSelection()
            }
        }

        override fun onViewAttachedToWindow(holder: AdapterViewHolder) {
            super.onViewAttachedToWindow(holder)
            holder.gridItemView.onAddedToParentView()
            holder.gridItemView.apply {
                if (!isDestructed) view.visible()
                loadRegistrations.resume()
            }
        }

        override fun onViewDetachedFromWindow(holder: AdapterViewHolder) {
            super.onViewDetachedFromWindow(holder)
            holder.gridItemView.onRemovedFromParentView()
            holder.gridItemView.apply {
                view.invisible()
                loadRegistrations.pause()
            }
        }

        override fun onViewRecycled(holder: AdapterViewHolder) {
            super.onViewRecycled(holder)
            unbind(holder)
        }

        override fun onFailedToRecycleView(holder: AdapterViewHolder): Boolean {
            unbind(holder)
            return false
        }

        private fun unbind(holder: CSGridView<ItemType, ViewType>.AdapterViewHolder) {
            holder.gridItemView.apply {
                view.invisible()
                loadRegistrations.clear()
            }
        }

        override fun getItemViewType(position: Int): Int = data[position].second
        override fun getItemCount() = data.size
    }
}

