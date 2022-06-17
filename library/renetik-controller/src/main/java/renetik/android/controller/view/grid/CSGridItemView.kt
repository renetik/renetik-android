package renetik.android.controller.view.grid

import android.view.ViewGroup
import androidx.core.view.children
import renetik.android.controller.base.CSView
import renetik.android.core.lang.CSLayoutRes
import renetik.android.extensions.activated
import renetik.android.extensions.selected

// Requires item to be wrapped in empty frame layout for now...
open class CSGridItemView<RowType : Any>(
    parent: CSView<out ViewGroup>,
    layout: CSLayoutRes,
    var onLoad: ((CSGridItemView<RowType>).(RowType) -> Unit)? = null)
    : CSView<ViewGroup>(parent, layout) {

    lateinit var value: RowType
    var index = -1
    var itemDisabled = false

    fun load(value: RowType, index: Int = 0) {
        this.index = index
        this.value = value
        onLoad(value)
    }

    open fun onLoad(value: RowType) {
        onLoad?.invoke(this, value)
    }

    override var isActivated: Boolean
        get() = view.children.first().isActivated
        set(value) {
            view.children.first().activated(value)
        }

    override var isSelected: Boolean
        get() = view.children.first().isSelected
        set(value) {
            view.children.first().selected(value)
        }
}

